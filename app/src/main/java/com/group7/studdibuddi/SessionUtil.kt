package com.group7.studdibuddi

import com.group7.studdibuddi.session.Session

object SessionUtil {

    // Filters:
    var publicOnly: Boolean = false
    var startTime: Long? = null
    var endTime: Long? = null
    var nameContain: String = ""

    fun notFiltered(session: Session): Boolean{
        if (!session.isPublic && publicOnly){
            return false
        }
        if (startTime != null) {
            if (session.startTime < startTime!!){ return false }
        }
        if (endTime != null) {
            if (session.endTime > endTime!!){ return false }
        }
        if (!session.sessionName.contains(nameContain)){ return false }

        return true
    }

    fun resetFilter(){
        publicOnly = false
        startTime = null
        endTime = null
        nameContain = ""
    }
}