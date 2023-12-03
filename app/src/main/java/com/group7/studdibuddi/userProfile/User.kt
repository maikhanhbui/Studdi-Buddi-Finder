package com.group7.studdibuddi.userProfile

data class User (

    var userId: String = "",

    var profilePictureUrl: String = "",

    var userName: String = "",

    var personalEmail: String = "", // Or other social media link

    var phoneNumber: String = "",

    var gender: Int = -1,

    var coursesEnrolled: String = "",

    var major: String = "",

    var joinedSessionIDs: String = ""
)