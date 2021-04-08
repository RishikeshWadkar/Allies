package com.rishikeshwadkar.socialapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.dao.PostDao
import com.rishikeshwadkar.socialapp.dao.UserDao
import com.rishikeshwadkar.socialapp.models.Post
import com.rishikeshwadkar.socialapp.models.User
import kotlinx.android.synthetic.main.item_view.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostAdapter(options: FirestoreRecyclerOptions<Post>, val listener: IPostAdapter) : FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(options) {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userName: TextView = itemView.userName
        val userCreatedAt: TextView = itemView.userCreatedAt
        val userImageView: ImageView = itemView.userImage
        val userPost: TextView = itemView.userPostText
        val userPostLikedOrNot: ImageView = itemView.userImageLikedOrNot
        val userLikeCount: TextView = itemView.userPostLikeCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val viewHolder = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false))
        viewHolder.userPostLikedOrNot.setOnClickListener {
            listener.likeButtonListener(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {

        updateFireStoreData(model)
        holder.userName.text = model.createdBy.userDisplayName
        holder.userCreatedAt.text = Utils.getTimeAgo(model.currentTime)
        Glide.with(holder.userImageView.context).load(model.createdBy.userImage).circleCrop().into(holder.userImageView)
        holder.userLikeCount.text = model.likedBy.size.toString()
        holder.userPost.text = model.text

        val currentUser = Firebase.auth.uid
        val isLiked = model.likedBy.contains(currentUser)

        if(isLiked){
            holder.userPostLikedOrNot.setImageResource(R.drawable.ic_liked)
        }
        else
            holder.userPostLikedOrNot.setImageResource(R.drawable.ic_unliked)
    }

    private fun updateFireStoreData(model: Post){
        val db = FirebaseFirestore.getInstance()
        val userDao = UserDao()
        val postUserName = model.createdBy.userDisplayName

        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userDao.getUserById(model.createdBy.uid).await().toObject(User::class.java)!!

            val tag: String= "test"
            if(postUserName != user.userDisplayName) {
                //Log.d(TAG, postUserName)
                Log.d(tag, user.userDisplayName)

                db.collection("posts").whereEqualTo("createdBy.userDisplayName", postUserName)
                        .whereEqualTo("currentTime", model.currentTime).get().addOnSuccessListener { documents ->
                            for (document in documents) {
                                Log.d(tag, "${document.id} ")
                                db.collection("posts").document(document.id).update("createdBy.userDisplayName", user.userDisplayName)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(tag, "Error getting documents: ", exception)
                        }
            }
        }

    }

    interface IPostAdapter{
        fun likeButtonListener(postID: String)
    }
}