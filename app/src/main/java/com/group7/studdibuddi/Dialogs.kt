package com.group7.studdibuddi

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class Dialogs: DialogFragment(), DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    companion object{
        // Dialog Keys
        const val DIALOG_KEY = "KEY"

        const val MAP_PICKER_KEY = 0
    }

    // Map picker variables:
    private var selectedLatLng: LatLng? = null

    private var locationPickerCallback: LocationPickerCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        val dialogId = bundle!!.getInt(DIALOG_KEY)
        var builder = AlertDialog.Builder(requireActivity())

        if (dialogId == MAP_PICKER_KEY) {
            builder = this.locationPickerDialog(builder)
        }
        // Other Dialog goes here


        val ret = builder.create()
        // TODO: Add smooth animation to the dialog rise from bottom
        //  ret.window?.attributes?.windowAnimations = R.style.DialogAnimationFromBottom

        return ret
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
            getFragmentManager()?.beginTransaction()?.remove(mapFragment)?.commit()
        }

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
        // Title of the dialog
        builder.setTitle("Pick Location")
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_map_picker, null)

        // Set the view first
        builder.setView(view)
        // Button action before the map is ready
        builder.setNegativeButton("Cancel", this)

        builder.setPositiveButton("OK") { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (selectedLatLng == null) {
                    Toast.makeText(requireContext(), "No location selected", Toast.LENGTH_SHORT)
                        .show()
                }
                else{
                    Toast.makeText(requireContext(), "Location selected", Toast.LENGTH_SHORT)
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
            val southwest = LatLng(49.270316, -122.931407) // Replace with actual southwest coordinates
            val northeast = LatLng(49.281851, -122.901690) // Replace with actual northeast coordinates
            val sfuBurnabyBounds = LatLngBounds(southwest, northeast)
            // Set padding if needed
            val padding = 100 // You can adjust this value based on your preference
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
        return builder
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