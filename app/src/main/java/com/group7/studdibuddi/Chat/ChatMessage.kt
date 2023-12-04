package com.group7.studdibuddi.Chat

data class ChatMessage(
    val sessionID: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)