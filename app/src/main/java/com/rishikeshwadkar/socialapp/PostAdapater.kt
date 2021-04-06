package com.rishikeshwadkar.socialapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.rishikeshwadkar.socialapp.models.Post
import kotlinx.android.synthetic.main.item_view.view.*

class PostAdapater(options: FirestoreRecyclerOptions<Post>) : FirestoreRecyclerAdapter<Post, PostAdapater.PostViewHolder>(options) {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userName: TextView = itemView.userName
        val userCreatedAt: TextView = itemView.userCreatedAt
        val userImageView: ImageView = itemView.userImage
        val userPost: TextView = itemView.userPostText
        val userPostLikedOrNot: ImageView = itemView.userImageLikedOrNot
        val userLikeCount: TextView = itemView.userPostLikeCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return PostViewHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        holder.userName.text = model.createdBy.userDisplayName
        holder.userCreatedAt.text = Utils.getTimeAgo(model.currentTime)
        Glide.with(holder.userImageView.context).load(model.createdBy.userImage).circleCrop().into(holder.userImageView)
        holder.userLikeCount.text = model.likedBy.size.toString()
        holder.userPost.text = model.text

    }
}