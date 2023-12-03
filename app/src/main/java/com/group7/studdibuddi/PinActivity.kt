package com.group7.studdibuddi

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.group7.studdibuddi.session.BaseActivity
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

    private lateinit var mapPickerButton: Button

    private lateinit var publicCheckBox: CheckBox

    private var isPickingLocation = false
    private var mapPickerDialog: Dialogs? = null

    private var selectLatLng: LatLng = DatabaseUtil.sfuLocation
    private lateinit var GIVE_A_NAME_TITLE: String
    private lateinit var LOCATION_SELECTED_TITLE: String


    private lateinit var selectedStartCalendar: Calendar

    private lateinit var selectedEndCalendar: Calendar

    private var datePickerShowing = false
    private var timePickerShowing = false
    private var isPickingStartTime = true

    private var isPublic = true
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

        mapPickerButton.setOnClickListener{ locationPicking() }

        GIVE_A_NAME_TITLE = getString(R.string.give_a_name)
        LOCATION_SELECTED_TITLE = getString(R.string.location_selected)

        saveButton.setOnClickListener {
            if (session_name.text.isEmpty()){
                Toast.makeText(this, GIVE_A_NAME_TITLE, Toast.LENGTH_SHORT).show()
            }
            else {
                DatabaseUtil.createSession(this,
                    session_name.text.toString(),
                    locationSpinner.selectedItemPosition,
                    selectLatLng,
                    session_course.text.toString(),
                    session_description.text.toString(),
                    isPublic,
                    selectedStartCalendar.timeInMillis,
                    selectedEndCalendar.timeInMillis){ success ->
                    if (success) {
                        finish()
                    } else {
                        //displays error messages
                    }
                }
            }
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

            selectLatLng = LatLng(savedInstanceState.getDouble("SELECTED_LAT", DatabaseUtil.sfuLocation.latitude),
            savedInstanceState.getDouble("SELECTED_LON", DatabaseUtil.sfuLocation.longitude))

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

        outState.putDouble("SELECTED_LAT", selectLatLng.latitude)
        outState.putDouble("SELECTED_LON", selectLatLng.longitude)

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
                val errorMessage = if (isStartTime) "Start time cannot be later than end time"
                else "End time cannot be earlier than start time"

                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (isStartTime) selectedStartCalendar = targetTime
                else selectedEndCalendar = targetTime

                this.updateTime()
                timePickDialog(isStartTime)
            }
            this.updateTime()
        }

        var datePickerDialog = DatePickerDialog(this)
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
                val errorMessage = if (isStartTime) "Start time cannot be later than end time"
                else "End time cannot be earlier than start time"

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
        startTimeText.text = "Start Time: " + dateFormat.format(selectedStartCalendar.time)
        endTimeText.text = "End Time: " + dateFormat.format(selectedEndCalendar.time)
    }

}