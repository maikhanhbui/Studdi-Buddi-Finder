package com.group7.studdibuddi.session

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group7.studdibuddi.DatabaseUtil
import com.group7.studdibuddi.Util

object SessionUtil {

    // Filters:
    var includePublic: Boolean = true
    var locationFilter: Int = 0
    private var startTime: Long? = null
    private var endTime: Long? = null
    var nameContain: String = ""
    var distanceRange: Double = 0.0


    var currentLatLng: LatLng? = null
    var selectedSession: Session? = null

    fun filterSessions(sessions: List<Session>): List<Session> {
        return sessions.filter { session ->
            if (session.isPublic && !includePublic) {
                return@filter false
            }
            // Forbid not logged in user to view public session
            if (!session.isPublic && DatabaseUtil.currentUser == null) {
                return@filter false
            }
            if (locationFilter > 0) {
                if (session.location != locationFilter - 1) {
                    return@filter false
                }
            }
            if (startTime != null) {
                if (session.startTime < startTime!!) {
                    return@filter false
                }
            }
            if (endTime != null) {
                if (session.endTime > endTime!!) {
                    return@filter false
                }
            }
            if (!session.sessionName.contains(nameContain)) {
                return@filter false
            }

            if (currentLatLng != null && distanceRange != 0.0){
                if (!isWithinRange(session.latitude,session.longitude)){
                    return@filter false
                }
            }

            // Include the session in the filtered list
            return@filter true
        }
    }

    private fun isWithinRange(lat: Double, lon: Double): Boolean{
        val distance = Util.calculateDistance(
            lat, lon,
            currentLatLng!!.latitude, currentLatLng!!.longitude
        )
        return (distance <= distanceRange)
    }

    var showOwnedOnly = false

    fun joinedSessions(sessions: List<Session>): List<Session> {
        return sessions.filter { session ->
            if (DatabaseUtil.currentUser != null) {
                if (session.ownerId == DatabaseUtil.currentUser!!.uid) {
                    return@filter true
                }
                if (!showOwnedOnly) {
                    if (session.usersJoined.contains(DatabaseUtil.currentUser?.uid!!)) {
                        return@filter true
                    }
                }
            }
            // Include the session in the filtered list
            return@filter false
        }
    }

    // Given list of user ids return their names
    fun joinedUser(usersJoined: MutableList<String>, ownerID: String, callback: (List<String>) -> Unit) {
        if (!usersJoined.contains(ownerID)) {
            usersJoined.add(ownerID)
        }
        val userRef = FirebaseDatabase.getInstance().getReference("users")
        val usernames: MutableList<String> = mutableListOf()
        usersJoined.forEach { userId ->
            val userQuery = userRef.child(userId)
            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("userName").getValue(String::class.java)
                    // Add the username to the list
                    username?.let {
                        usernames.add(it)
                    }
                    // If all usernames are retrieved, invoke the callback
                    if (usernames.size == usersJoined.size) {
                        callback(usernames)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }


    fun resetFilter(){
        includePublic = true
        locationFilter = 0
        startTime = null
        endTime = null
        nameContain = ""
        distanceRange = 0.0
        Log.d("filter", "filter reset")
    }
}