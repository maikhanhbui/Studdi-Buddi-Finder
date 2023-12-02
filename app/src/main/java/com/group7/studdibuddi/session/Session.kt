package com.group7.studdibuddi.session

import java.text.DateFormat
import java.util.Calendar
import java.util.Date

data class Session (

    var sessionId: Long = 0L,

    var sessionKey: String = "",

    var sessionName: String = "",

    var courseId: String = "",

    var location: Int = -1,

    var latitude: Double = 49.279,

    var longitude: Double = -122.918,

    var description: String = "",

    var ownerId: String = "",

    var joinedUserIds: String = "",

    var isPublic: Boolean = false,

    var startTime: Long = System.currentTimeMillis(),

    var endTime: Long = System.currentTimeMillis()+(1000*600)
)