package com.rishikeshwadkar.socialapp.data.models

data class Post(

        val text: String = "",
        val currentTime: Long = 0L,
        val createdBy: User = User(),
        val likedBy: ArrayList<String> = ArrayList()
)

