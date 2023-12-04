package com.group7.studdibuddi.session

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

    var usersJoined: MutableList<String> = mutableListOf(),

    var isPublic: Boolean = false,

    var startTime: Long = System.currentTimeMillis(),

    var endTime: Long = System.currentTimeMillis()+(1000*600),

    var imageURL: String = ""

)