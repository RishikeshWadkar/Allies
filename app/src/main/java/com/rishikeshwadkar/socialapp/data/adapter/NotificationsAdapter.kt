package com.rishikeshwadkar.socialapp.data.adapter

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
import com.rishikeshwadkar.socialapp.data.dao.NotificationsDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Notification
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.notifications_item_view.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class NotificationsAdapter(options: FirestoreRecyclerOptions<Notification>, private val listener: NotificationListener):
        FirestoreRecyclerAdapter<Notification, NotificationsAdapter.NotificationsViewHolder>(options) {

    private val userDao = UserDao()
    private val notificationsDao = NotificationsDao()

    class NotificationsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.notification_profile_image
        var notificationText: TextView = itemView.notification_noti_text
        val notificationBtn: TextView = itemView.notification_button
        val notificationItemView: ConstraintLayout = itemView.notification_item_view_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val viewHolder = NotificationsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notifications_item_view, parent, false))
        viewHolder.notificationBtn.setOnClickListener {
            listener.onNotificationButtonClickListener(
                    snapshots.getSnapshot(viewHolder.adapterPosition).id,
                    viewHolder.adapterPosition)
        }

        viewHolder.userImage.setOnClickListener {
            GlobalScope.launch {
                val notification = notificationsDao
                    .getNotificationById(snapshots.getSnapshot(viewHolder.adapterPosition).id)
                    .await().toObject(Notification::class.java)
                withContext(Dispatchers.Main){
                    listener.onProfileClickListener(notification!!.from, notification.to)
                }
            }
        }
        viewHolder.notificationItemView.setOnClickListener {
            listener.onNotificationClickListener(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }

        viewHolder.notificationText.setOnClickListener {
            listener.onNotificationClickListener(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int, model: Notification) {
        holder.notificationText.text = model.notificationText

        dataSetter(model, holder)
    }

    private fun dataSetter(model: Notification, holder: NotificationsViewHolder){
        if (model.type == "Like"){
            holder.notificationBtn.visibility = View.INVISIBLE
            GlobalScope.launch(Dispatchers.IO) {
                val fromUser: User = userDao.getUserById(model.from).await().toObject(User::class.java)!!
                withContext(Dispatchers.Main){
                    Glide.with(holder.userImage).load(fromUser.userImage).circleCrop().into(holder.userImage)
                }
            }
        }
        else if (model.from == Firebase.auth.currentUser!!.uid){
            GlobalScope.launch(Dispatchers.IO) {
                val oppositeUser = userDao.getUserById(model.to).await().toObject(User::class.java)!!
                withContext(Dispatchers.Main){
                    Glide.with(holder.userImage).load(oppositeUser.userImage).circleCrop().into(holder.userImage)
                    holder.notificationText.text = "Your request to ${oppositeUser.userDisplayName} is pending"
                    holder.notificationBtn.visibility = View.VISIBLE
                    holder.notificationBtn.text = "Pending"
                    holder.notificationBtn.setBackgroundResource(R.drawable.add_button_shape)
                }
            }
        }
        else{
            GlobalScope.launch(Dispatchers.IO) {
                val fromUser: User = userDao.getUserById(model.from).await().toObject(User::class.java)!!
                withContext(Dispatchers.Main){
                    Glide.with(holder.userImage).load(fromUser.userImage).circleCrop().into(holder.userImage)
                }
            }
            holder.notificationBtn.visibility = View.VISIBLE
            holder.notificationBtn.text = "Accept"
            holder.notificationBtn.setBackgroundResource(R.drawable.add_button_shape)
        }
    }


    interface NotificationListener{
        fun onNotificationButtonClickListener(notificationId: String, position: Int)
        fun onProfileClickListener(fromUid: String, toUid: String)
        fun onNotificationClickListener(notificationId: String)
    }
}