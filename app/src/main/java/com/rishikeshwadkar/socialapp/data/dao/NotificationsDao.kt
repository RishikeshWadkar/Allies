package com.rishikeshwadkar.socialapp.data.dao

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.data.models.Notification
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationsDao {
    private val db = FirebaseFirestore.getInstance()
    val currentUser: FirebaseUser? = Firebase.auth.currentUser
    val notificationsCollection = db.collection("notifications")
    val userDao: UserDao = UserDao()
    var duplicateCounter: Int = 0


    fun addLikeNotification(notification: Notification){
        GlobalScope.launch(Dispatchers.IO) {
            notificationsCollection.document().set(notification).await()
        }
    }

    fun removeNotification(notificationId: String){
        GlobalScope.launch(Dispatchers.IO) {
            notificationsCollection.document(notificationId).delete()
        }
    }

    fun getNotificationById(uid: String): Task<DocumentSnapshot>{
        return notificationsCollection.document(uid).get()
    }

    fun addAddToAllieRequestsNotification(notification: Notification){
        GlobalScope.launch {
            notificationsCollection.document().set(notification).await()
        }
    }

}