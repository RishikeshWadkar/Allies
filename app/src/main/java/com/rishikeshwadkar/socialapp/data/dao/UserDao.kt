package com.rishikeshwadkar.socialapp.data.dao

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDao {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")

    fun addUser(user: User){
        user.let {
            GlobalScope.launch(Dispatchers.IO) {
                userCollection.document(user.uid).set(it)
            }
        }
    }

    fun updatePassword(uId: String, pass: String){
        GlobalScope.launch(Dispatchers.IO) {
            userCollection.document(uId).update("userPassword", pass)
            Log.d("confirm", "userDao password updated!!!")
        }
    }

    fun updatePostCount(uid: String){
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userCollection.document(uid).get().await().toObject(User::class.java)!!
            userCollection.document(uid).update("userPostCount",user.userPostCount + 1)
        }
    }

    fun getUserById(uId: String): Task<DocumentSnapshot> {
        return userCollection.document(uId).get()
    }

}