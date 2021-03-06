package com.rishikeshwadkar.socialapp.data.dao

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.navigation.NavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.PostAdapter
import com.rishikeshwadkar.socialapp.data.models.Notification
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import com.rishikeshwadkar.socialapp.fragments.post.AlliesPostFragmentDirections
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

    fun addPost(text: String, navController: NavController, viewModel: MyViewModel){
        val currentUserId = auth.currentUser!!.uid
        GlobalScope.launch(Dispatchers.IO) {
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime: Long = System.currentTimeMillis()

            val post = Post(text, currentTime, user)
            postCollection.document().set(post)
                    .addOnSuccessListener {
                        viewModel.dismissDialog()
                        navController.navigate(R.id.action_addPostFragment_to_postsFragment)
                    }
        }
    }

    fun getPostByID(postID: String): Task<DocumentSnapshot>{
        return postCollection.document(postID).get()
    }

    fun goToUserByPostId(postId: String, navController: NavController, goFrom: String){
        GlobalScope.launch(Dispatchers.IO) {
            val post: Post = getPostByID(postId).await().toObject(Post::class.java)!!
            withContext(Dispatchers.Main){
                Log.d("gettingUid",post.createdBy.uid)
                if(post.createdBy.uid == Firebase.auth.currentUser!!.uid){
                    if (goFrom == "post")
                        navController.navigate(R.id.action_postsFragment_to_myProfileFragment)
                    else if (goFrom == "allies")
                        navController.navigate(R.id.action_alliesPostFragment_to_myProfileFragment)
                }
                else{
                    if (goFrom == "post"){
                        val action = PostsFragmentDirections.actionPostsFragmentToUserProfileFragment(post.createdBy.uid)
                        navController.navigate(action)
                    }
                    else if (goFrom == "allies"){
                        val action = AlliesPostFragmentDirections.actionAlliesPostFragmentToUserProfileFragment(post.createdBy.uid)
                        navController.navigate(action)
                    }

                }

            }
        }
    }

    fun updateLike(postID: String, adapter: PostAdapter?, position: Int){
        GlobalScope.launch(Dispatchers.IO) {
            val post = getPostByID(postID).await().toObject(Post::class.java)
            val liked = post?.likedBy?.contains(Firebase.auth.uid)
            val user: User = userDao.getUserById(Firebase.auth.uid!!).await().toObject(User::class.java)!!

            if(liked!!){
                post.likedBy.remove(Firebase.auth.uid)
                GlobalScope.launch {
                    notificationsDao.notificationsCollection.whereEqualTo("to", post.createdBy.uid)
                            .whereEqualTo("from", Firebase.auth.currentUser!!.uid)
                            .orderBy("currentTime", Query.Direction.DESCENDING)
                            .get().addOnSuccessListener { documents ->
                                var tempCounter: Int = 0
                                for (document in documents){
                                    if (tempCounter > 0) break
                                    notificationsDao.notificationsCollection.document(document.id).delete()
                                    tempCounter++
                                }
                            }
                }
            }
            else{
                post.likedBy.add(Firebase.auth.uid!!)
                val currentTime: Long = System.currentTimeMillis()

                if (post.createdBy.uid != user.uid){
                    val notification: Notification = Notification(
                            "Like",
                            user.uid,
                            post.createdBy.uid,
                            "${user.userDisplayName} liked your post",
                            currentTime,
                            postID
                    )
                    notificationsDao.addLikeNotification(notification)
                }
            }
            postCollection.document(postID).update("likedBy",post.likedBy).addOnSuccessListener {
                adapter?.notifyItemChanged(position)
            }
        }
    }
}