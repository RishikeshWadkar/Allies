package com.rishikeshwadkar.socialapp.data.adapter

import android.graphics.Color
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
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.NotificationsDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Notification
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.notifications_item_view.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.zip.Inflater
import kotlin.jvm.internal.Ref

class NotificationsAdapter(options: FirestoreRecyclerOptions<Notification>, private val listener: NotificationListener):
        FirestoreRecyclerAdapter<Notification, NotificationsAdapter.NotificationsViewHolder>(options) {

    private val userDao = UserDao()
    private val notificationsDao = NotificationsDao()

    class NotificationsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.notification_profile_image
        var notificationText: TextView = itemView.notification_noti_text
        val notificationBtn: TextView = itemView.notification_button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val viewHolder = NotificationsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notifications_item_view, parent, false))
        viewHolder.notificationBtn.setOnClickListener {
            listener.onNotificationButtonClickListener(
                    snapshots.getSnapshot(viewHolder.adapterPosition).id,
                    viewHolder.adapterPosition)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int, model: Notification) {
        holder.notificationText.text = model.notificationText

        GlobalScope.launch(Dispatchers.IO) {
            val fromUser: User = userDao.getUserById(model.from).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main){
                Glide.with(holder.userImage).load(fromUser.userImage).circleCrop().into(holder.userImage)
            }
        }
        btnSetter(model, holder)
    }

    private fun btnSetter(model: Notification, holder: NotificationsViewHolder){
        if (model.type == "Like"){
            holder.notificationBtn.visibility = View.INVISIBLE
        }
        else{
            holder.notificationBtn.visibility = View.VISIBLE
            holder.notificationBtn.text = "Accept"
            holder.notificationBtn.setBackgroundResource(R.drawable.add_button_shape)
        }
    }


    interface NotificationListener{
        fun onNotificationButtonClickListener(notificationId: String, position: Int)
    }
}