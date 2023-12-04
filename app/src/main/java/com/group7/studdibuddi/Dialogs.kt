package com.group7.studdibuddi

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.group7.studdibuddi.session.SessionUtil

class Dialogs: DialogFragment(), DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    companion object{
        // Dialog Keys
        const val DIALOG_KEY = "KEY"

        const val MAP_PICKER_KEY = 0

        const val FILTER_KEY = 1
    }

    // Map picker variables:
    private var selectedLatLng: LatLng? = null

    private var locationPickerCallback: LocationPickerCallback? = null

    private lateinit var LOCATION_SELECTED_TITLE: String
    private lateinit var CANCEL_BUTTON_TITLE: String
    private lateinit var DIALOG_TITLE: String
    private lateinit var NO_LOCATION_TITLE: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        val dialogId = bundle!!.getInt(DIALOG_KEY)
        var builder = AlertDialog.Builder(requireActivity())

        if (dialogId == MAP_PICKER_KEY) {
            builder = this.locationPickerDialog(builder)
        } else if (dialogId == FILTER_KEY) {
            this.filterDialog(builder)
        }

        return builder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, id: Int) {
        // Set the button behaviour
        if (id == DialogInterface.BUTTON_NEGATIVE) {
            dialog?.cancel()
        }
        if (id == DialogInterface.BUTTON_POSITIVE) {
            dialog?.cancel()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        // Clean up the map after
        val mapFragment = requireFragmentManager().findFragmentById(R.id.picker_map) as SupportMapFragment?
        if (mapFragment != null){
            fragmentManager?.beginTransaction()?.remove(mapFragment)?.commit()
        }
        onDismissListener?.invoke()
        super.onDismiss(dialog)
    }

    override fun onCancel(dialog: DialogInterface) {
        // Close the dialog for the use of screen rotation (only onDismiss is called)
        // This allow caller activity can set on cancel listener of the dialogs it has called
        (activity as? DialogInterface.OnCancelListener)?.onCancel(dialog)

        super.onCancel(dialog)
    }

    // Dialogs goes below
    private fun locationPickerDialog(builder: AlertDialog.Builder): AlertDialog.Builder {
        LOCATION_SELECTED_TITLE = getString(R.string.location_selected)
        CANCEL_BUTTON_TITLE = getString(R.string.cancel_button)
        DIALOG_TITLE = getString(R.string.pick_location)
        NO_LOCATION_TITLE = getString(R.string.no_location_selected)
        // Title of the dialog
        builder.setTitle(DIALOG_TITLE)
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_map_picker, null)

        // Set the view first
        builder.setView(view)
        // Button action before the map is ready
        builder.setNegativeButton(CANCEL_BUTTON_TITLE, this)

        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (selectedLatLng == null) {
                    Toast.makeText(requireContext(), NO_LOCATION_TITLE, Toast.LENGTH_SHORT)
                        .show()
                }
                else{
                    Toast.makeText(requireContext(), LOCATION_SELECTED_TITLE, Toast.LENGTH_SHORT)
                        .show()
                    // Put the location in call back
                    selectedLatLng?.let {
                        locationPickerCallback?.onLocationPicked(it)
                    }
                }
                dialog?.cancel()
            }
        }

        // Find and replace the FrameLayout with the SupportMapFragment
        val mapFragment = requireFragmentManager().findFragmentById(R.id.picker_map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            // Placeholder coordinates for SFU Burnaby
            val southwest = LatLng(49.270316, -122.931407)
            val northeast = LatLng(49.281851, -122.901690)
            val sfuBurnabyBounds = LatLngBounds(southwest, northeast)
            // Set padding if needed
            val padding = 100
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(sfuBurnabyBounds, padding))

            googleMap.setOnMapClickListener { latLng ->
                // Clear existing markers
                googleMap.clear()
                // Place a marker at the clicked location
                googleMap.addMarker(MarkerOptions().position(latLng))
                // Update the selectedLatLng
                selectedLatLng = latLng
            }
        }
        builder.setNegativeButton(CANCEL_BUTTON_TITLE, this)
        return builder
    }

    private fun filterDialog(builder: AlertDialog.Builder): AlertDialog.Builder {
        CANCEL_BUTTON_TITLE = getString(R.string.close)
        DIALOG_TITLE = getString(R.string.filter)
        NO_LOCATION_TITLE = getString(R.string.no_location_selected)
        // Title of the dialog
        builder.setTitle(DIALOG_TITLE)
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_filter, null)

        val publicCheckBox = view.findViewById<CheckBox>(R.id.checkBoxPublic)
        publicCheckBox.isChecked = SessionUtil.includePublic
        publicCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SessionUtil.includePublic = isChecked
        }

        val locationSpinner = view.findViewById<Spinner>(R.id.spinnerLocation)
        locationSpinner.setSelection(SessionUtil.locationFilter)
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                SessionUtil.locationFilter = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val editLocationRange = view.findViewById<EditText>(R.id.editTextLocationRange)
        editLocationRange.setText(SessionUtil.distanceRange.toString())
        editLocationRange.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    SessionUtil.distanceRange = s.toString().toDouble()
                } else{
                    SessionUtil.distanceRange = 0.0}
            }
        })

        val editTextNameContains = view.findViewById<EditText>(R.id.editTextNameContains)
        editTextNameContains.setText(SessionUtil.nameContain)
        editTextNameContains.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    SessionUtil.nameContain = s.toString()
                }else{
                    SessionUtil.nameContain = ""}
            }
        })

        builder.setView(view)

        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->

        }
        .setNegativeButton(getString(R.string.reset)){ _, _ ->
                SessionUtil.resetFilter()
        }

        return builder
    }

    private var onDismissListener: (() -> Unit)? = null

    // Setter method to set the onDismissListener
    fun setOnDismissListener(listener: () -> Unit) {
        this.onDismissListener = listener
    }



    // To pass the selected latLng back to the activity
    interface LocationPickerCallback {
        fun onLocationPicked(latLng: LatLng)
    }
    // Setter method to set the callback
    fun setLocationPickerCallback(callback: LocationPickerCallback) {
        this.locationPickerCallback = callback
    }

}