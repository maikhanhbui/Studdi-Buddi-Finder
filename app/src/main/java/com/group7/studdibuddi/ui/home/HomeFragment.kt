package com.group7.studdibuddi.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group7.studdibuddi.DatabaseUtil
import com.group7.studdibuddi.R
import com.group7.studdibuddi.session.Session
import com.group7.studdibuddi.databinding.FragmentHomeBinding
import com.group7.studdibuddi.session.SessionListAdapter
import com.group7.studdibuddi.session.SessionViewModel
import com.group7.studdibuddi.session.SessionViewModelFactory
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import java.lang.Exception

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    private lateinit var gMap: GoogleMap

    private lateinit var sessionViewModel: SessionViewModel
    private lateinit var sessionListAdapter: SessionListAdapter
    private lateinit var viewModelFactory: SessionViewModelFactory

    private val binding get() = _binding!!

    val markerMap = HashMap<String, Marker>()

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


        // Session List:
        viewModelFactory = SessionViewModelFactory()
        sessionViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(SessionViewModel::class.java)

        sessionListAdapter = SessionListAdapter(requireActivity(), emptyList())

        sessionViewModel.allSessionLiveData.observe(viewLifecycleOwner) { sessions ->
            // Update when observe changes
            sessionListAdapter.replace(sessions)
            sessionListAdapter.notifyDataSetChanged()
        }

        binding.sessionList.adapter = sessionListAdapter

        binding.sessionList.setOnItemClickListener { _, _, position, _ ->
            try {
                val targetKey = sessionListAdapter.getItem(position).sessionKey
                Log.d("marker", "key:$targetKey")
                val targetMarker = markerMap[targetKey]
                if (targetMarker != null){
                    // Draw back the slider
                    binding.sessionPullUp.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    // Move the camera towards the target marker
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(offSetLocation(targetMarker.position), 17f)
                    gMap.animateCamera(cameraUpdate)
                    // Start the target dialog
                    showSessionDialog(targetKey, targetMarker)
                }
                else{
                    Toast.makeText(requireActivity(), "Marker not found", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                Toast.makeText(requireActivity(), "Error: $e", Toast.LENGTH_SHORT).show()
            }
        }

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

        gMap.setOnMarkerClickListener { marker ->
            // Move the camera towards the target marker
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(offSetLocation(marker.position), 17f)
            gMap.animateCamera(cameraUpdate)
            marker.title?.let { showSessionDialog(it, marker) }
            true
        }
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
                markerMap.clear()

                for (sessionSnapshot in dataSnapshot.children) {
                    sessionSnapshot.key.toString()
                    val sessionKey = sessionSnapshot.key.toString()
                    val session = sessionSnapshot.getValue(Session::class.java)
                    session?.let {
                        val sessionLatLng = LatLng(it.latitude, it.longitude)
                        // TODO: Session click shows a detailed session page (ideally dialog), in there
                        //  we have all the information and actions we can perform (join, contact, etc.)
                        // Set pin with title of the session name
                        // Modified to establish relationship between marker and session
                        val newMarker = gMap.addMarker(MarkerOptions().position(sessionLatLng).title(sessionKey))
                        markerMap[sessionKey] = newMarker!!
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

    // Temporary function for session dialog and session deletion
    // Compare the currentUser with ownerId so that only the owner can view and delete
    private fun showSessionDialog(sessionId: String, marker: Marker) {
        val sessionsRef = FirebaseDatabase.getInstance().getReference("session")
        sessionsRef.child(sessionId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val session = snapshot.getValue(Session::class.java)
                session?.let {
                    if (DatabaseUtil.currentUser?.uid == it.ownerId) {
                        // Current user is the owner, show delete option
                        val dialogBuilder = AlertDialog.Builder(context)
                        val locationId = it.location
                        val locationString = getLocationStringFromInt(locationId)
                        dialogBuilder.setTitle(it.sessionName)
                        dialogBuilder.setMessage("\nLocation: ${locationString ?: "Unknown Location"}\nDescription: ${it.description ?: "No description available"}\nCourse: ${it.courseId ?: "No course ID available"}")
                        dialogBuilder.setPositiveButton("Delete") { dialog, _ ->
                            deleteSessionFromDatabase(sessionId, marker)
                            dialog.dismiss()
                        }
                        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                        dialogBuilder.create().show()

                    } else {
                        // Current user is not the owner, show message
                        //Toast.makeText(context, "You are not the owner of this session", Toast.LENGTH_SHORT).show()
                        val dialogBuilder = AlertDialog.Builder(context)
                        val locationId = it.location
                        val locationString = getLocationStringFromInt(locationId)
                        dialogBuilder.setTitle(it.sessionName)
                        dialogBuilder.setMessage("\nLocation: ${locationString ?: "Unknown Location"}\nDescription: ${it.description ?: "No description available"}\nCourse: ${it.courseId ?: "No course ID available"}")

                        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        dialogBuilder.create().show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(sessionId: String, marker: Marker) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Delete Confirmation")
        dialogBuilder.setMessage("Are you sure you want to delete this session?")
        dialogBuilder.setPositiveButton("Delete") { dialog, _ ->
            deleteSessionFromDatabase(sessionId, marker)
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.create().show()
    }

    fun getLocationStringFromInt(locationId: Int): String {
        return when (locationId) {
            0 -> "AQ"
            1 -> "AAB"
            2 -> "ASB"
            3 -> "BEE"
            4 -> "BFC"
            5 -> "T3"
            6 -> "BLU"
            7 -> "CCC"
            8 -> "CML"
            9 -> "CSTN"
            10 -> "DAC"
            11 -> "DIS1"
            12 -> "DIS2"
            13 -> "ECC"
            else -> "Unknown Location"
        }
    }

    // Function to delete session from database and the corresponding marker
    private fun deleteSessionFromDatabase(sessionId: String, marker: Marker) {
        val sessionsRef = FirebaseDatabase.getInstance().getReference("session")
        sessionsRef.child(sessionId).removeValue()
            .addOnSuccessListener {
                // Successfully deleted, now remove the marker
                marker.remove()
                Toast.makeText(context, "Session deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle failure, e.g., show a toast
                Toast.makeText(context, "Failed to delete session: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    val METERS_PER_DEGREE_LATITUDE = 111319.9

    // Offset the view location to south in order to view the pin better
    fun offSetLocation(location: LatLng): LatLng {
        val originalLocation = Location("original_location")
        originalLocation.latitude = location.latitude
        originalLocation.longitude = location.longitude

        val offsetDistance = 100.0 // offset amount in meters
        val offsetLocation = Location(originalLocation)
        offsetLocation.latitude =
            originalLocation.latitude - (offsetDistance / METERS_PER_DEGREE_LATITUDE)
        return LatLng(offsetLocation.latitude, offsetLocation.longitude)
    }
}