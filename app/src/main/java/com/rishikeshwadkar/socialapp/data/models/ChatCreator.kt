package com.rishikeshwadkar.socialapp.data.models

data class ChatCreator(
        val usersUid: ArrayList<String> = ArrayList(),
        val latestChat: Chat = Chat()
)
