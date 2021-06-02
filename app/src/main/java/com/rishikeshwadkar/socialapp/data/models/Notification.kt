package com.rishikeshwadkar.socialapp.data.models

data class Notification(
        val type: String = "",
        val from: String = "",
        val to: String = "",
        val notificationText: String = "",
        val currentTime: Long = 0L,
        val postId: String = "",
        val seen: Boolean = false
)
