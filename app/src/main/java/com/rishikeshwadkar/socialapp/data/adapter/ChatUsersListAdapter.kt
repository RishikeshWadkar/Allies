package com.rishikeshwadkar.socialapp.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.users_item_view.view.*

class ChatUsersListAdapter(private val userArray: ArrayList<User>, val listener: OnUserClicked): RecyclerView.Adapter<ChatUsersListAdapter.ChatUsersViewHolder>() {

    class ChatUsersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userImage: ImageView = itemView.users_item_image
        val userName: TextView = itemView.users_item_name
        val userEmail: TextView = itemView.users_item_email
        val layoutView: ConstraintLayout = itemView.userItemViewConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUsersViewHolder {
        val viewHolder = ChatUsersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.users_item_view, parent, false))
        viewHolder.layoutView.setOnClickListener {
            listener.onUserClicked(userArray[viewHolder.adapterPosition].uid)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ChatUsersViewHolder, position: Int) {
        holder.userName.text = userArray[position].userDisplayName
        holder.userEmail.text = userArray[position].userEmail
        Glide.with(holder.userImage).load(userArray[position].userImage).into(holder.userImage)
    }

    override fun getItemCount(): Int {
        return userArray.size
    }

    interface OnUserClicked{
        fun onUserClicked(uid: String)
    }

}