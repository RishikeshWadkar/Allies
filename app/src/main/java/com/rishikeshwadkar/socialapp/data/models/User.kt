package com.rishikeshwadkar.socialapp.data.models

data class User(
        val uid : String = "",
        var userDisplayName : String = "",
        val userImage : String = "",
        val userEmail : String = "",
        val userPhoneNo : String = "",
        val userPassword : String = "",
        val userPostCount : Int = 0,
        val myAllies: ArrayList<String> = ArrayList()
)

