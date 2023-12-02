package com.group7.studdibuddi

import android.util.Log
import com.group7.studdibuddi.session.Session

object SessionFilter {

    // Filters:
    var includePublic: Boolean = true
    var locationFilter: Int = 0
    var startTime: Long? = null
    var endTime: Long? = null
    var nameContain: String = ""
    var distanceFilter: Double = 0.0

    fun filterSessions(sessions: List<Session>): List<Session> {
        return sessions.filter { session ->
            if (!session.isPublic && !includePublic) {
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

            // Include the session in the filtered list
            return@filter true
        }
    }

    fun isWithinRange(lat: Double, lon: Double){

    }


    fun resetFilter(){
        includePublic = true
        locationFilter = 0
        startTime = null
        endTime = null
        nameContain = ""
        distanceFilter = 0.0
        Log.d("filter", "filter resetted")
    }
}