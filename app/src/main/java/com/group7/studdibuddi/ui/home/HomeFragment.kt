package com.group7.studdibuddi.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group7.studdibuddi.R
import com.group7.studdibuddi.Session
import com.group7.studdibuddi.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    private lateinit var gMap: GoogleMap

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize the map fragment
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        // Set the callback for when the map is ready
        mapFragment.getMapAsync(this)

        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        // Ensure map is fully loaded before manipulating it
        googleMap.setOnMapLoadedCallback {
            // Set the initial camera position to show the entire SFU Burnaby area

            // Placeholder coordinates for SFU Burnaby
            val southwest = LatLng(49.270316, -122.931407)
            val northeast = LatLng(49.281851, -122.901690)

            val sfuBurnabyBounds = LatLngBounds(southwest, northeast)

            // Set padding if needed
            val padding = 100

            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(sfuBurnabyBounds, padding))
        }

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation(googleMap)
        } else {
            // Request location permissions
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Start fetching sessions and update the map
        this.updateSessionData()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location on the map
                mapFragment.getMapAsync { googleMap ->
                    enableMyLocation(googleMap)
                }
            } else {
                // Permission denied, handle it accordingly (e.g., show a message to the user)
                // You may want to disable certain features that require location
            }
        }
    }

    private fun enableMyLocation(googleMap: GoogleMap) {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Enable the "My Location" layer on the map
            googleMap.isMyLocationEnabled = true

            // Get the last known location and add a marker
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.addMarker(MarkerOptions().position(userLatLng).title("Your Location"))
                }
            }
        } else {
            // You may want to handle the case where permission is not granted
            // Show a message or request permission again
            // You can also disable certain features that require location
            // For example, you might disable the "My Location" layer or show a message to the user
        }
    }

    private fun updateSessionData() {
        val sessionsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("session")

        sessionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear existing markers
                gMap.clear()

                for (sessionSnapshot in dataSnapshot.children) {
                    sessionSnapshot.key.toString()
                    val session = sessionSnapshot.getValue(Session::class.java)
                    session?.let {
                        val sessionLatLng = LatLng(it.latitude, it.longitude)
                        // TODO: Session click shows a detailed session page (ideally dialog), in there
                        //  we have all the information and actions we can perform (join, contact, etc.)
                        // Set pin with title of the session name
                        gMap.addMarker(MarkerOptions().position(sessionLatLng).title(it.sessionName))
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // TODO: Handle error
                // Handle errors here
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}