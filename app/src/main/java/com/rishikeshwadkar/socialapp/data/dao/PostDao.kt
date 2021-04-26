package com.rishikeshwadkar.socialapp.data.dao

import android.util.Log
import android.view.View
import androidx.navigation.Navigation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.models.Notification
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.fragments.post.PostsFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostDao {
    private val db = FirebaseFirestore.getInstance()
    val postCollection = db.collection("posts")
    private val auth = Firebase.auth
    private val notificationsDao = NotificationsDao()
    private val userDao = UserDao()

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

    fun getUserUidByPostId(postId: String, view: View){
        GlobalScope.launch(Dispatchers.IO) {
            val post: Post = getPostByID(postId).await().toObject(Post::class.java)!!
            withContext(Dispatchers.Main){
                Log.d("gettingUid",post.createdBy.uid)
                if(post.createdBy.uid == Firebase.auth.currentUser!!.uid)
                    Navigation.findNavController(view).navigate(R.id.action_postsFragment_to_myProfileFragment)
                else{
                    val action = PostsFragmentDirections.actionPostsFragmentToUserProfileFragment(post.createdBy.uid)
                    Navigation.findNavController(view).navigate(action)
                }

            }
        }
    }

    fun updateLike(postID: String){
        GlobalScope.launch(Dispatchers.IO) {
            val post = getPostByID(postID).await().toObject(Post::class.java)
            val liked = post?.likedBy?.contains(Firebase.auth.uid)
            val user: User = userDao.getUserById(Firebase.auth.uid!!).await().toObject(User::class.java)!!

            if(liked!!){
                post.likedBy.remove(Firebase.auth.uid)
                GlobalScope.launch {
                    notificationsDao.notificationsCollection.whereEqualTo("uid", post.createdBy.uid)
                            .whereEqualTo("postId", postID).get().addOnSuccessListener { documents ->
                                for (document in documents){
                                    notificationsDao.notificationsCollection.document(document.id).delete()
                                }
                            }
                }
            }
            else{
                post.likedBy.add(Firebase.auth.uid!!)
                val currentTime: Long = System.currentTimeMillis()
                val notification: Notification = Notification(
                        post.createdBy.uid,
                        user.userImage,
                        "${user.userDisplayName} liked your post",
                        currentTime,
                        postID,
                        Firebase.auth.currentUser!!.uid
                )
                notificationsDao.addLikeNotification(notification)
            }

            postCollection.document(postID).update("likedBy",post.likedBy)
        }
    }
}