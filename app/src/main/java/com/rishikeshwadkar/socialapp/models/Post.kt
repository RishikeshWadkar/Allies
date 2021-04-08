package com.rishikeshwadkar.socialapp.models

import com.google.firebase.firestore.Exclude

data class Post(

    val text: String = "",
    val currentTime: Long = 0L,
    val createdBy: User = User(),
    val likedBy: ArrayList<String> = ArrayList()
)

