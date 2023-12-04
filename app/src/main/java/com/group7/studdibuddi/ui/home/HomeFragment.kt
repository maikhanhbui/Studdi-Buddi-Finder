package com.group7.studdibuddi.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group7.studdibuddi.DatabaseUtil
import com.group7.studdibuddi.Dialogs
import com.group7.studdibuddi.R
import com.group7.studdibuddi.Util
import com.group7.studdibuddi.session.SessionUtil
import com.group7.studdibuddi.session.Session
import com.group7.studdibuddi.databinding.FragmentHomeBinding
import com.group7.studdibuddi.session.SessionListAdapter
import com.group7.studdibuddi.session.SessionViewModel
import com.group7.studdibuddi.session.SessionViewModelFactory
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    private lateinit var locationCallback: LocationCallback

    private lateinit var gMap: GoogleMap

    private lateinit var sessionViewModel: SessionViewModel
    private lateinit var sessionListAdapter: SessionListAdapter
    private lateinit var viewModelFactory: SessionViewModelFactory

    private lateinit var DELETE_BUTTON_TITLE: String
    private lateinit var CANCEL_BUTTON_TITLE: String
    private lateinit var LOCATION_BUTTON_TITLE: String
    private lateinit var DESCRIPTION_BUTTON_TITLE: String
    private lateinit var COURSE_BUTTON_TITLE: String
    private lateinit var START_TIME_BUTTON_TITLE: String
    private lateinit var END_TIME_BUTTON_TITLE: String
    private lateinit var DELETE_SUCCESS_TITLE: String
    private lateinit var DELETE_NOT_SUCCESS_TITLE: String
    private lateinit var MARKER_NOT_FOUND_TITLE: String

    private val binding get() = _binding!!

    val markerMap = HashMap<String, Marker>()

    private var isFiltering = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        DELETE_BUTTON_TITLE = getString(R.string.delete_button)
        CANCEL_BUTTON_TITLE = getString(R.string.cancel_button)
        LOCATION_BUTTON_TITLE = getString(R.string.session_location)
        DESCRIPTION_BUTTON_TITLE = getString(R.string.description)
        COURSE_BUTTON_TITLE = getString(R.string.session_course)
        START_TIME_BUTTON_TITLE = getString(R.string.session_start_time)
        END_TIME_BUTTON_TITLE = getString(R.string.session_end_time)
        DELETE_SUCCESS_TITLE = getString(R.string.delete_successfully)
        DELETE_NOT_SUCCESS_TITLE = getString(R.string.delete_unsuccessfully)
        MARKER_NOT_FOUND_TITLE = getString(R.string.marker_not_found)

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize the map fragment
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        // Set the callback for when the map is ready
        mapFragment.getMapAsync(this)


        // Session List:
        viewModelFactory = SessionViewModelFactory()
        sessionViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(SessionViewModel::class.java)
//        sessionViewModel.fetchData()

        sessionListAdapter = SessionListAdapter(requireActivity(), emptyList())


        // All using session livedata, observing on filtered data
        // Observe the filter sessions

        // Set the observe to update pin


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
                    Toast.makeText(requireActivity(), MARKER_NOT_FOUND_TITLE, Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                Toast.makeText(requireActivity(), "Error: $e", Toast.LENGTH_SHORT).show()
            }
        }

        // Hide the panel when click outside
        binding.sessionPullUp.setFadeOnClickListener {
            if ( binding.sessionPullUp.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                binding.sessionPullUp.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
        }

        binding.filterButton.setOnClickListener{
            filterDialog()
        }

        return root
    }

    private fun filterDialog(){
        val filterDialog = Dialogs()
        val bundle = Bundle()
        isFiltering = true
        bundle.putInt(Dialogs.DIALOG_KEY, Dialogs.FILTER_KEY)
        filterDialog.arguments = bundle

        filterDialog.setOnDismissListener {
            sessionViewModel.updateFilter()
        }
        filterDialog.show(childFragmentManager, "filter_dialog")
    }

    private fun pinUpdate(sessions: List<Session>) {
        if (::gMap.isInitialized) {
            // Clear existing markers
            gMap.clear()
            markerMap.clear()

            for (session in sessions) {
                val sessionKey = session.sessionKey
                val sessionLatLng = LatLng(session.latitude, session.longitude)

                // Set pin with title of the session name
                val customMarkerView = layoutInflater.inflate(R.layout.custom_marker, null)
                val imageView = customMarkerView.findViewById<ImageView>(R.id.markerImageView)
                val textView = customMarkerView.findViewById<TextView>(R.id.markerTextView)

                // Set the text
                textView.text = session.sessionName
                imageView.setImageResource(R.drawable.sfu_logo)

                // Load the image asynchronously with Picasso
                if (session.imageURL.isNotEmpty()) {
                    Picasso.get().load(session.imageURL).into(imageView, object : Callback {
                        override fun onSuccess() {
                            // Image loaded successfully, create and add the marker to the map
                            val markerOptions = MarkerOptions()
                                .position(sessionLatLng)
                                .title(sessionKey)
                                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(customMarkerView)))

                            val newMarker = gMap.addMarker(markerOptions)
                            markerMap[sessionKey] = newMarker!!
                        }

                        override fun onError(e: Exception?) {
                            // Handle error loading image
                            Log.e("Picasso", "Error loading image for session $sessionKey", e)
                        }
                    })
                } else {
                    // If no image URL is provided, create and add the marker without an image
                    val markerOptions = MarkerOptions()
                        .position(sessionLatLng)
                        .title(sessionKey)
                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(customMarkerView)))

                    val newMarker = gMap.addMarker(markerOptions)
                    markerMap[sessionKey] = newMarker!!
                }
            }
        }
    }


    private fun createDrawableFromView(view: View): Bitmap {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        sessionViewModel.filteredSessionLiveData.observe(viewLifecycleOwner) { sessions ->
            // Update when observe changes
            this.pinUpdate(sessions)
            sessionListAdapter.replace(sessions)
            sessionListAdapter.notifyDataSetChanged()
        }

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
//        this.updateSessionData()

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
//                    val userLatLng = LatLng(it.latitude, it.longitude)
//                    googleMap.addMarker(MarkerOptions().position(userLatLng).title(LOCATION_BUTTON_TITLE))
                }
            }
            // Create a location request to set the priority and interval
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)     // Passive interval: 10sec
                .setFastestInterval(5000)   // Min interval: 5sec

            // Create a location callback
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    // Get the current LatLng from the location result
                    val location = locationResult.lastLocation
                    if (location != null) {
                        SessionUtil.currentLatLng = LatLng(location.latitude, location.longitude)
                    }
                    if (::sessionViewModel.isInitialized) {
//                        sessionViewModel.updateFilter()
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // You may want to handle the case where permission is not granted
            // Show a message or request permission again
            // You can also disable certain features that require location
            // For example, you might disable the "My Location" layer or show a message to the user
        }
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
                        dialogBuilder.setMessage("\n$LOCATION_BUTTON_TITLE: ${locationString}\n$DESCRIPTION_BUTTON_TITLE: ${it.description}\n$COURSE_BUTTON_TITLE: ${it.courseId}\n" +
                                "$START_TIME_BUTTON_TITLE: ${Util.timeStampToTimeString(it.startTime)}\n" +
                                "$END_TIME_BUTTON_TITLE: ${Util.timeStampToTimeString(it.endTime)}")

                        dialogBuilder.setNeutralButton(getString(R.string.show_group)) { _, _ ->
                            // Fetch user details based on the user IDs in usersJoined list
                            val usersJoined = it.usersJoined

                            if (usersJoined.isNotEmpty()) {
                                // Fetch user details for each user ID
                                val userDetailsList = ArrayList<String>()

                                val remainingUserDetails = usersJoined.size

                                for (userId in usersJoined) {
                                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            val userName = userSnapshot.child("userName").getValue(String::class.java)
                                            val userEmail = userSnapshot.child("personalEmail").getValue(String::class.java)

                                            if (userName != null && userEmail != null) {
                                                val userDetails = "$userName - $userEmail"
                                                userDetailsList.add(userDetails)
                                            }

                                            // Display the user details once all data is fetched
                                            if (userDetailsList.size == remainingUserDetails) {
                                                val userGroupString = userDetailsList.joinToString("\n")

                                                // Create a custom dialog to display user details
                                                val dialogView = layoutInflater.inflate(R.layout.layout_custom_user_details, null)
                                                val textUserDetails = dialogView.findViewById<TextView>(R.id.textUserDetails)

                                                // Set the user details in the TextView
                                                textUserDetails.text = "${getString(R.string.users_in_group)}:\n$userGroupString"

                                                // Create and show the custom dialog
                                                val customDialog = AlertDialog.Builder(context)
                                                    .setView(dialogView)
                                                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                                                        // Handle the OK button click if needed
                                                    }
                                                    .create()

                                                customDialog.show()
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            // Handle error if needed
                                        }
                                    })
                                }
                            } else {
                                // Handle case when the usersJoined list is empty
                                Toast.makeText(context, getString(R.string.no_users_in_the_group), Toast.LENGTH_SHORT).show()
                            }
                        }

                        dialogBuilder.setPositiveButton(DELETE_BUTTON_TITLE) { dialog, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                deleteSessionFromDatabase(sessionId, marker)
                            }
                            dialog.dismiss()
                        }
                        dialogBuilder.setNegativeButton(CANCEL_BUTTON_TITLE) { dialog, _ -> dialog.dismiss() }
                        dialogBuilder.create().show()

                    } else {
                        // Current user is not the owner, show message
                        //Toast.makeText(context, "You are not the owner of this session", Toast.LENGTH_SHORT).show()

                        val currentUser = DatabaseUtil.currentUser
                        val isUserJoined = currentUser != null && it.usersJoined.contains(DatabaseUtil.currentUser?.uid!!)

                        val dialogBuilder = AlertDialog.Builder(context)
                        val locationId = it.location
                        val locationString = getLocationStringFromInt(locationId)
                        dialogBuilder.setTitle(it.sessionName)
                        dialogBuilder.setMessage("\n$LOCATION_BUTTON_TITLE: ${locationString}\n$DESCRIPTION_BUTTON_TITLE: ${it.description}\n$COURSE_BUTTON_TITLE: ${it.courseId}")

                        if (isUserJoined) {
                            // Current user is already joined, show "Leave" button
                            dialogBuilder.setNeutralButton(getString(R.string.leave)) { dialog, _ ->
                                // Remove the current user from joinedUserIds list
                                it.usersJoined.remove(DatabaseUtil.currentUser?.uid)
                                // Save the updated session to Firebase
                                sessionsRef.child(sessionId).setValue(it)

                                Toast.makeText(context, getString(R.string.successfully_left_group), Toast.LENGTH_SHORT).show()

                                // Add your logic for "Leave group" action here
                                dialog.dismiss()
                            }
                        } else if (DatabaseUtil.currentUser != null) {
                            // Current user is not joined, show "Join" button
                            dialogBuilder.setNeutralButton(getString(R.string.join)) { dialog, _ ->
                                // Add the current user to joinedUserIds list
                                it.usersJoined.add(DatabaseUtil.currentUser?.uid ?: "")
                                // Save the updated session to Firebase
                                sessionsRef.child(sessionId).setValue(it)

                                Toast.makeText(context,
                                    getString(R.string.successfully_joined_group), Toast.LENGTH_SHORT).show()

                                // Add your logic for "Join group" action here
                                dialog.dismiss()
                            }
                        }

                        dialogBuilder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
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
            else -> "NA"
        }
    }

    // Function to delete session from database and the corresponding marker
    private suspend fun deleteSessionFromDatabase(sessionId: String, marker: Marker) {
        try {
            val sessionsRef = FirebaseDatabase.getInstance().getReference("session")
            sessionsRef.child(sessionId).removeValue().await()
            withContext(Dispatchers.Main) {
                marker.remove()
                Toast.makeText(context, DELETE_SUCCESS_TITLE, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "$DELETE_NOT_SUCCESS_TITLE: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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