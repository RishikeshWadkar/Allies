package com.rishikeshwadkar.socialapp.data.adapter

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
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.notifications_item_view.view.*

class AlliesUserListAdapter(options: FirestoreRecyclerOptions<User>, val listener: AlliesUserListInterface) :
    FirestoreRecyclerAdapter<User, AlliesUserListAdapter.AlliesUserListViewHolder>(options) {

    class AlliesUserListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userImage: ImageView = itemView.notification_profile_image
        val userName: TextView = itemView.notification_noti_text
        val removeBtn: TextView = itemView.notification_button
        var oppositeUid: String = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlliesUserListViewHolder {
        val viewHolder = AlliesUserListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notifications_item_view, parent, false))
        viewHolder.removeBtn.setOnClickListener {
            listener.onRemoveBtnClickListener(viewHolder.oppositeUid)
        }
        viewHolder.userImage.setOnClickListener {
            listener.onUserClickListener(viewHolder.oppositeUid)
        }
        viewHolder.userName.setOnClickListener {
            listener.onUserClickListener(viewHolder.oppositeUid)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: AlliesUserListViewHolder, position: Int, model: User) {
        holder.oppositeUid = model.uid
        holder.userName.text = model.userDisplayName
        holder.userName.textSize = 18F
        holder.removeBtn.setBackgroundResource(R.drawable.add_button_shape)
        Glide.with(holder.userImage).load(model.userImage).into(holder.userImage)

        when {
            model.uid == Firebase.auth.currentUser!!.uid -> {
                holder.removeBtn.visibility = View.GONE
            }
            model.userAllies.contains(Firebase.auth.currentUser!!.uid) -> {
                holder.removeBtn.visibility = View.VISIBLE
                holder.removeBtn.text = "Remove"
            }
            model.userRequests.contains(Firebase.auth.currentUser!!.uid) -> {
                holder.removeBtn.visibility = View.VISIBLE
                holder.removeBtn.text = "Request Sent"
            }
            model.userRequestSent.contains(Firebase.auth.currentUser!!.uid) -> {
                holder.removeBtn.visibility = View.VISIBLE
                holder.removeBtn.text = "Accept"
            }
            else -> {
                holder.removeBtn.visibility = View.VISIBLE
                holder.removeBtn.text = "Add"
            }
        }
    }

    interface AlliesUserListInterface{
        fun onRemoveBtnClickListener(uid: String)
        fun onUserClickListener(uid: String)
    }
}