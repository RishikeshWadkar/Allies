package com.rishikeshwadkar.socialapp.data.models

data class Chat(
        val from: String = "",
        val to: String = "",
        val message: String = "",
        val msgTime: Long = 0L
)
