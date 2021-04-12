package com.rishikeshwadkar.socialapp.data.dao

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {
    private val db = FirebaseFirestore.getInstance()
    val postCollection = db.collection("posts")
    private val auth = Firebase.auth

    fun addPost(text: String){
        val currentUserId = auth.currentUser!!.uid
        GlobalScope.launch(Dispatchers.IO) {
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime: Long = System.currentTimeMillis()

            val post = Post(text, currentTime, user)
            postCollection.document().set(post)
        }
    }

    private fun getPostByID(postID: String): Task<DocumentSnapshot>{
        return postCollection.document(postID).get()
    }

    fun updateLike(postID: String){
        GlobalScope.launch(Dispatchers.IO) {
            val currentUser = auth.currentUser!!.uid
            val post = getPostByID(postID).await().toObject(Post::class.java)
            val liked = post?.likedBy?.contains(currentUser)

            if(liked!!){
                post.likedBy.remove(currentUser)
            }
            else{
                post.likedBy.add(currentUser)
            }

            postCollection.document(postID).update("likedBy",post.likedBy)
        }
    }
}