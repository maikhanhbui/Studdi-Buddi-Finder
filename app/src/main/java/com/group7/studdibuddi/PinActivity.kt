package com.group7.studdibuddi

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng

class PinActivity: AppCompatActivity(), DialogInterface.OnCancelListener, Dialogs.LocationPickerCallback  {
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var locationSpinner: Spinner

    private lateinit var session_name: EditText
    private lateinit var session_course: EditText
    private lateinit var session_description: EditText

    private lateinit var mapPickerButton: Button

    private var isPickingLocation = false
    private var mapPickerDialog: Dialogs? = null

    private var selectLatLng: LatLng = DatabaseUtil.sfuLocation

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

        mapPickerButton.setOnClickListener{ locationPicking() }

        saveButton.setOnClickListener {
            if (session_name.text.isEmpty()){
                Toast.makeText(this, "Give your session a name", Toast.LENGTH_SHORT).show()
            }
            else {
                DatabaseUtil.createSession(this,
                    session_name.text.toString(),
                    locationSpinner.selectedItemPosition,
                    selectLatLng,
                    session_course.text.toString(),
                    session_description.text.toString()){ success ->
                    if (success) {
                        // TODO: HANDLE CASE USER CLICK SAVE TOO MANY TIMES MIGHT RESULT IN DUPLICATES
                        finish()
                    } else {
                        //displays error messages
                    }
                }
            }
        }
        cancelButton.setOnClickListener { finish() }
        setupSpinners()

        if (savedInstanceState != null) {
            session_name.setText(savedInstanceState.getString("SESSION_NAME", ""))
            session_course.setText(savedInstanceState.getString("SESSION_COURSE", ""))
            locationSpinner.setSelection(savedInstanceState.getInt("LOCATION_SPINNER"))
            session_description.setText(savedInstanceState.getString("SESSION_DESCRIPTION", ""))

            isPickingLocation = savedInstanceState.getBoolean("IS_PICKING_LOCATION", false)

            selectLatLng = LatLng(savedInstanceState.getDouble("SELECTED_LAT", DatabaseUtil.sfuLocation.latitude),
            savedInstanceState.getDouble("SELECTED_LON", DatabaseUtil.sfuLocation.longitude))
        }
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
        mapPickerDialog!!.setLocationPickerCallback(this)
        mapPickerDialog!!.arguments = bundle
        mapPickerDialog!!.show(supportFragmentManager, "map_picker_dialog")
    }

    override fun onLocationPicked(latLng: LatLng) {
        selectLatLng = latLng
        Log.d(TAG, "location selected: $selectLatLng")
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
    }

    // Code to execute when the dialog is cancelled instead of dismissed
    override fun onCancel(dialog: DialogInterface) {

        isPickingLocation = false
    }

}