package com.group7.studdibuddi

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class PinActivity: AppCompatActivity() {
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var locationSpinner: Spinner
    private lateinit var session_id: EditText
    private lateinit var session_name: EditText
    private lateinit var session_course: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_activity)

        session_id = findViewById(R.id.session_id)
        session_name = findViewById(R.id.session_name)
        session_course = findViewById(R.id.session_course)
        saveButton = findViewById(R.id.save_session_button)
        cancelButton = findViewById(R.id.cancel_session_button)
        locationSpinner = findViewById(R.id.location_spinner)

        saveButton.setOnClickListener { finish() }
        cancelButton.setOnClickListener { finish() }
        setupSpinners()

        if (savedInstanceState != null) {
            session_id.setText(savedInstanceState.getString("SESSION_ID", ""))
            session_name.setText(savedInstanceState.getString("SESSION_NAME", ""))
            session_course.setText(savedInstanceState.getString("SESSION_COURSE", ""))
            locationSpinner.setSelection(savedInstanceState.getInt("LOCATION_SPINNER"))
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

    // Save the state of the input
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SESSION_ID", session_id.text.toString())
        outState.putString("SESSION_NAME", session_name.text.toString())
        outState.putString("SESSION_COURSE", session_course.text.toString())
        outState.putInt("LOCATION_SPINNER", locationSpinner.selectedItemPosition)
    }
}