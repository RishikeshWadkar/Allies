package com.rishikeshwadkar.socialapp.data.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.ChatCreator
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.users_item_view.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class ChatChatUsersListAdapter(options: FirestoreRecyclerOptions<ChatCreator>, private val listener: UserClickInterface)
    : FirestoreRecyclerAdapter<ChatCreator, ChatChatUsersListAdapter.ChatChatViewHolder>(options) {

    val userDao = UserDao()

    class ChatChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val userImage: ImageView  = itemView.users_item_image
        val userName : TextView = itemView.users_item_name
        val userLatestMessage: TextView = itemView.users_item_email
        var userUid: String = ""
        val layoutView: ConstraintLayout = itemView.userItemViewConstraintLayout
        val redBadge: ImageView = itemView.user_item_red_badge
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatChatViewHolder {
        val viewHolder = ChatChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.users_item_view, parent, false))
        viewHolder.layoutView.setOnClickListener {
            listener.onUserClickListener(viewHolder.userUid)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ChatChatViewHolder, position: Int, model: ChatCreator) {
        CoroutineScope(Dispatchers.IO).launch {
            var oppositeUserId = if (model.usersUid[0] == Firebase.auth.currentUser!!.uid) model.usersUid[1]
            else model.usersUid[0]
            val oppositeUser: User = userDao.getUserById(oppositeUserId).await().toObject(User::class.java)!!

            withContext(Dispatchers.Main){
                holder.userName.text = oppositeUser.userDisplayName
                Glide.with(holder.userImage).load(oppositeUser.userImage).into(holder.userImage)
                holder.userLatestMessage.text = model.latestChat.message
                holder.userUid = oppositeUserId

                if ( (!model.latestChat.seen) && model.latestChat.to == Firebase.auth.currentUser!!.uid){
                    holder.userLatestMessage.typeface = Typeface.DEFAULT_BOLD
                    holder.redBadge.visibility = View.VISIBLE
                }
                else{
                    holder.userLatestMessage.typeface = Typeface.DEFAULT
                    holder.redBadge.visibility = View.GONE
                }
            }
        }
    }

    interface UserClickInterface{
        fun onUserClickListener(chatCreatorUid: String)
    }
}