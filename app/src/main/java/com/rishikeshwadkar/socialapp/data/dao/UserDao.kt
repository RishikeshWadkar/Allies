package com.rishikeshwadkar.socialapp.data.dao

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.rishikeshwadkar.socialapp.data.adapter.NotificationsAdapter
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserDao {

    val db = FirebaseFirestore.getInstance()
    val userCollection = db.collection("users")

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

    fun updateName(uid: String, newName: String){
        GlobalScope.launch(Dispatchers.IO) {
            userCollection.document(uid).update("userDisplayName",newName)
        }
    }

    fun updatePhone(uid: String, phoneNo: String){
        GlobalScope.launch(Dispatchers.IO) {
            userCollection.document(uid).update("userPhoneNo", phoneNo)
        }
    }

    fun updatePhoto(uid: String, photoUrl: String){
        GlobalScope.launch(Dispatchers.IO) {
            userCollection.document(uid).update("userImage", photoUrl)
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

    fun addRequest(uid: String, requesterUid: String, adapter: NotificationsAdapter, position: Int){
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userCollection.document(uid).get().await().toObject(User::class.java)!!
            val oppositeUser: User = userCollection.document(requesterUid).get().await().toObject(User::class.java)!!
            user.userRequestSent.add(requesterUid)
            oppositeUser.userRequests.add(uid)
            userCollection.document(uid).set(user)
            userCollection.document(requesterUid).set(oppositeUser)
            withContext(Dispatchers.Main){
                adapter.notifyItemChanged(position)
            }
        }
    }

    fun addToAllies(uid: String, likerUid: String, adapter: NotificationsAdapter, position: Int){
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userCollection.document(uid).get().await().toObject(User::class.java)!!
            val oppositeUser: User = userCollection.document(likerUid).get().await().toObject(User::class.java)!!
            user.userRequests.remove(likerUid)
            user.userAllies.add(likerUid)
            oppositeUser.userRequestSent.remove(uid)
            oppositeUser.userAllies.add(uid)
            userCollection.document(uid).set(user)
            userCollection.document(likerUid).set(oppositeUser)
            withContext(Dispatchers.Main){
                adapter.notifyItemChanged(position)
            }
        }
    }

}