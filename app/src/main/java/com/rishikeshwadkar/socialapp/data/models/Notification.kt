package com.rishikeshwadkar.socialapp.data.models

data class Notification(
        val uid: String = "",
        val userImage: String = "",
        val notificationText: String = "",
        val currentTime: Long = 0L,
        val postId: String = "",
        val likerUid: String = ""
)
