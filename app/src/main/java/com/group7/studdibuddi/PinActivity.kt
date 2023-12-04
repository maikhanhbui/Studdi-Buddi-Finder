package com.group7.studdibuddi

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.group7.studdibuddi.session.BaseActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar

class PinActivity: BaseActivity(), DialogInterface.OnCancelListener, Dialogs.LocationPickerCallback  {
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var locationSpinner: Spinner

    private lateinit var session_name: EditText
    private lateinit var session_course: EditText
    private lateinit var session_description: EditText

    private lateinit var startTimeText: TextView
    private lateinit var startTimePickerButton: Button
    private lateinit var endTimeText: TextView
    private lateinit var endTimePickerButton: Button

    private lateinit var imageView: ImageView
    private lateinit var pickImageButton: Button

    private lateinit var mapPickerButton: Button

    private lateinit var publicCheckBox: CheckBox

    private var isPickingLocation = false
    private var mapPickerDialog: Dialogs? = null

    private var selectLatLng: LatLng? = null
    private lateinit var GIVE_A_NAME_TITLE: String
    private lateinit var LOCATION_SELECTED_TITLE: String
    private lateinit var PICK_A_LOCATION_TITLE: String


    private lateinit var selectedStartCalendar: Calendar

    private lateinit var selectedEndCalendar: Calendar

    private var datePickerShowing = false
    private var timePickerShowing = false
    private var isPickingStartTime = true

    private var isPublic = true

    private lateinit var SELECT_IMAGE_TITLE: String

    private lateinit var profilePictureUri: Uri
    private lateinit var tempProfilePictureUri: Uri
    private lateinit var pickedProfilePictureUri: Uri
    private lateinit var profilePicture: File
    private lateinit var tempProfilePicture: File
    private lateinit var pickedProfilePicture: File
    private val profilePictureName = "pfp.jpg"
    private val tempProfilePictureName = "temp_pfp.jpg"
    private val pickedProfilePictureName = "picked_pfp.jpg"

    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>

    private lateinit var pinViewModel: PinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_activity)

        session_name = findViewById(R.id.session_name)
        session_course = findViewById(R.id.session_course)
        session_description = findViewById(R.id.session_description_entry)

        saveButton = findViewById(R.id.save_session_button)
        cancelButton = findViewById(R.id.cancel_session_button)
        locationSpinner = findViewById(R.id.location_spinner)
        mapPickerButton = findViewById(R.id.map_picker_button)

        startTimeText = findViewById(R.id.start_time_text)
        startTimePickerButton = findViewById(R.id.start_time_picker)

        endTimeText = findViewById(R.id.end_time_text)
        endTimePickerButton = findViewById(R.id.end_time_picker)

        publicCheckBox = findViewById(R.id.check_box_public)

        pickImageButton = findViewById(R.id.pick_button)

        imageView = findViewById(R.id.session_photo)
        SELECT_IMAGE_TITLE = getString(R.string.select_profile_image)


        //Get uri for temp profile picture
        tempProfilePicture = File(getExternalFilesDir(null), tempProfilePictureName)
        tempProfilePictureUri = FileProvider.getUriForFile(this, "com.group7.studdibuddi", tempProfilePicture)

        pickedProfilePicture = File(getExternalFilesDir(null), pickedProfilePictureName)
        pickedProfilePictureUri = FileProvider.getUriForFile(this, "com.group7.studdibuddi", tempProfilePicture)

        mapPickerButton.setOnClickListener{ locationPicking() }

        GIVE_A_NAME_TITLE = getString(R.string.give_a_name)
        PICK_A_LOCATION_TITLE = getString(R.string.pick_a_location)
        LOCATION_SELECTED_TITLE = getString(R.string.location_selected)

        saveButton.setOnClickListener {
            if (session_name.text.isEmpty()){
                Toast.makeText(this, GIVE_A_NAME_TITLE, Toast.LENGTH_SHORT).show()
            }
            else if (selectLatLng == null){
                Toast.makeText(this, PICK_A_LOCATION_TITLE, Toast.LENGTH_SHORT).show()
            }
            else {
                DatabaseUtil.createSession(this,
                    session_name.text.toString(),
                    locationSpinner.selectedItemPosition,
                    selectLatLng!!,
                    session_course.text.toString(),
                    session_description.text.toString(),
                    isPublic,
                    selectedStartCalendar.timeInMillis,
                    selectedEndCalendar.timeInMillis){ sessionKey ->
                    if (sessionKey != null) {
                        // If session is created correctly, then upload its image
                        if (tempProfilePicture.exists()) {
                            Toast.makeText(this, " Uploading image", Toast.LENGTH_SHORT).show()
                            // Wait for call back to close, otherwise the file might be deleted while uploading
                            DatabaseUtil.uploadImageToFirebaseStorage(tempProfilePictureUri, sessionKey){success->
                                finish()
                            }
                        }
                        else {
                            finish()
                        }
                    } else {
                        //displays error messages
                    }
                }
            }
        }

        pinViewModel = ViewModelProvider(this)[PinViewModel::class.java]
        // Load the picture
        pinViewModel.userImage.observe(this) { it ->
            imageView.setImageBitmap(it)
        }

        if(tempProfilePicture.exists()) {
            imageView.setImageBitmap(Util.getBitmap(this, tempProfilePictureUri))
        }

        //Set cameraResult to temporary profile picture
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap = Util.getBitmap(this, tempProfilePictureUri)
                pinViewModel.userImage.value = bitmap
                MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, tempProfilePictureName, null)
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                //save Uri picked to tempProfilePicture
                tempProfilePictureUri = result.data?.data!!
                val bitmap = Util.getBitmap(this, tempProfilePictureUri)
                val fOut = FileOutputStream(tempProfilePicture)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
                fOut.flush()
                fOut.close()
                //set imageview
                pinViewModel.userImage.value = Util.getBitmap(this, tempProfilePictureUri)
            }
        }

        pickImageButton.setOnClickListener {
            val items = resources.getStringArray(R.array.change_profile_picture)
            val alertDialogBuilder = AlertDialog.Builder(this)
            var intent: Intent
            alertDialogBuilder.setTitle(SELECT_IMAGE_TITLE)
                .setItems(items) { _, index -> when(index)
                {
                    0 -> {
                        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempProfilePictureUri)
                        cameraResult.launch(intent)
                    }
                    1 -> {
                        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                        galleryResult.launch(intent)
                    }
                }
                }
            alertDialogBuilder.show()
        }

        cancelButton.setOnClickListener { finish() }
        setupSpinners()

        selectedStartCalendar = Calendar.getInstance()
        selectedEndCalendar = Calendar.getInstance()
        // Set the end time to 1 hour later
        selectedEndCalendar.timeInMillis = selectedStartCalendar.timeInMillis + 3600000

        startTimePickerButton.setOnClickListener{
            datePickDialog(true)
        }

        endTimePickerButton.setOnClickListener{
            datePickDialog(false)
        }

        if (savedInstanceState != null) {
            session_name.setText(savedInstanceState.getString("SESSION_NAME", ""))
            session_course.setText(savedInstanceState.getString("SESSION_COURSE", ""))
            locationSpinner.setSelection(savedInstanceState.getInt("LOCATION_SPINNER"))
            session_description.setText(savedInstanceState.getString("SESSION_DESCRIPTION", ""))

            isPickingLocation = savedInstanceState.getBoolean("IS_PICKING_LOCATION", false)

            val restoredLat = savedInstanceState.getDouble("SELECTED_LAT", -1.0)
            val restoredLon = savedInstanceState.getDouble("SELECTED_LON", -1.0)
            if (restoredLat != -1.0 && restoredLon != -1.0) {
                selectLatLng = LatLng(restoredLat, restoredLon)
            }

            selectedStartCalendar.timeInMillis = savedInstanceState.getLong("start_time", System.currentTimeMillis())
            selectedEndCalendar.timeInMillis = savedInstanceState.getLong("end_time", System.currentTimeMillis())


            isPickingStartTime = savedInstanceState.getBoolean("is_picking_start_time", true)
            if(savedInstanceState.getBoolean("date_picker_showing", false)){
                datePickDialog(isPickingStartTime)
            }
            if(savedInstanceState.getBoolean("time_picker_showing", false)){
                timePickDialog(isPickingStartTime)
            }

            isPublic = savedInstanceState.getBoolean("is_public", true)
        }

        publicCheckBox.isChecked = isPublic

        publicCheckBox.setOnCheckedChangeListener { _, isChecked ->
            isPublic = isChecked
        }

        this.updateTime()
        // Continue to show dialog
        if (isPickingLocation){ locationPicking() }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the image
        if (tempProfilePicture.exists()) {
            tempProfilePicture.delete()
        }
    }

    private fun setupSpinners() {
        val locationAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.location_list,
            android.R.layout.simple_spinner_dropdown_item
        )
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner.adapter = locationAdapter
    }

    // Start a dialog with map which allows user to choose a location
    private fun locationPicking(){
        mapPickerDialog = Dialogs()
        val bundle = Bundle()
        isPickingLocation = true
        bundle.putInt(Dialogs.DIALOG_KEY, Dialogs.MAP_PICKER_KEY)

        // Pass session information to the dialog
        bundle.putString("SESSION_NAME", session_name.text.toString())
        mapPickerDialog!!.setLocationPickerCallback(this)
        mapPickerDialog!!.arguments = bundle
        mapPickerDialog!!.show(supportFragmentManager, "map_picker_dialog")
    }

    override fun onLocationPicked(latLng: LatLng) {
        selectLatLng = latLng
        Log.d(TAG, "$LOCATION_SELECTED_TITLE: $selectLatLng")
    }

    override fun onPause() {
        // Dismiss the dialog in the case of screen rotation
        mapPickerDialog?.dismiss()
        super.onPause()
    }

    // Save the state of the input
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SESSION_NAME", session_name.text.toString())
        outState.putString("SESSION_COURSE", session_course.text.toString())
        outState.putInt("LOCATION_SPINNER", locationSpinner.selectedItemPosition)
        outState.putString("SESSION_DESCRIPTION", session_description.text.toString())

        outState.putBoolean("IS_PICKING_LOCATION", isPickingLocation)

        selectLatLng?.let { outState.putDouble("SELECTED_LAT", it.latitude) }
        selectLatLng?.let { outState.putDouble("SELECTED_LON", it.longitude) }

        // Save picker dialog state to keep the picker consistent during rotation
        outState.putBoolean("date_picker_showing", datePickerShowing)
        outState.putBoolean("time_picker_showing", timePickerShowing)
        outState.putBoolean("is_picking_start_time", isPickingStartTime)

        // Save the selected time
        outState.putLong("start_time", selectedStartCalendar.timeInMillis)
        outState.putLong("end_time", selectedEndCalendar.timeInMillis)

        outState.putBoolean("is_public", isPublic)
    }

    // Code to execute when the dialog is cancelled instead of dismissed
    override fun onCancel(dialog: DialogInterface) {

        isPickingLocation = false
    }


    private fun datePickDialog(isStartTime: Boolean){
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val targetTime = if (isStartTime) selectedStartCalendar.clone() as Calendar
            else selectedEndCalendar.clone() as Calendar

            targetTime.set(Calendar.YEAR, year)
            targetTime.set(Calendar.MONTH, monthOfYear)
            targetTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val comparisonResult = targetTime.compareTo(if (isStartTime) selectedEndCalendar else selectedStartCalendar)

            if ((isStartTime && comparisonResult > 0) || (!isStartTime && comparisonResult < 0)) {
                val errorMessage = if (isStartTime) getString(R.string.start_time_cannot_be_later_than_end_time)
                else getString(R.string.end_time_cannot_be_earlier_than_start_time)

                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (isStartTime) selectedStartCalendar = targetTime
                else selectedEndCalendar = targetTime

                this.updateTime()
                timePickDialog(isStartTime)
            }
            this.updateTime()
        }

        var datePickerDialog: DatePickerDialog
        if (isStartTime) {
            datePickerDialog = DatePickerDialog(
                this, dateSetListener,
                // Set the current date as default date
                selectedStartCalendar.get(Calendar.YEAR),
                selectedStartCalendar.get(Calendar.MONTH),
                selectedStartCalendar.get(Calendar.DAY_OF_MONTH)
            )
        } else{
            datePickerDialog = DatePickerDialog(
                this, dateSetListener,
                // Set the current date as default date
                selectedEndCalendar.get(Calendar.YEAR),
                selectedEndCalendar.get(Calendar.MONTH),
                selectedEndCalendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        datePickerDialog.setOnDismissListener{
            datePickerShowing = false
        }
        datePickerDialog.show()
        datePickerShowing = true
    }

    private fun timePickDialog(isStartTime: Boolean) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val targetCalendar = if (isStartTime) selectedStartCalendar.clone() as Calendar
            else selectedEndCalendar.clone() as Calendar

            targetCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            targetCalendar.set(Calendar.MINUTE, minute)
            targetCalendar.set(Calendar.SECOND, 0)

            val comparisonResult = targetCalendar.compareTo(if (isStartTime) selectedEndCalendar else selectedStartCalendar)

            if ((isStartTime && comparisonResult > 0) || (!isStartTime && comparisonResult < 0)) {
                val errorMessage = if (isStartTime) getString(R.string.start_time_cannot_be_later_than_end_time)
                else getString(R.string.end_time_cannot_be_earlier_than_start_time)

                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (isStartTime) selectedStartCalendar = targetCalendar
                else selectedEndCalendar = targetCalendar
            }
            this.updateTime()
        }


        var timePickerDialog = TimePickerDialog(
            this, timeSetListener,
            // Set the current time as default date
            selectedEndCalendar.get(Calendar.HOUR_OF_DAY),
            selectedEndCalendar.get(Calendar.MINUTE), true // Using 24 hours scheme
        )
        if (isStartTime) {
            timePickerDialog = TimePickerDialog(
                this, timeSetListener,
                // Set the current time as default date
                selectedStartCalendar.get(Calendar.HOUR_OF_DAY),
                selectedStartCalendar.get(Calendar.MINUTE), true // Using 24 hours scheme
            )
        }

        timePickerDialog.setOnDismissListener{
            timePickerShowing = false
        }
        timePickerDialog.show()
        timePickerShowing = true
    }

    private fun updateTime(){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        startTimeText.text = getString(R.string.start_time) + dateFormat.format(selectedStartCalendar.time)
        endTimeText.text = getString(R.string.end_time) + dateFormat.format(selectedEndCalendar.time)
    }

}