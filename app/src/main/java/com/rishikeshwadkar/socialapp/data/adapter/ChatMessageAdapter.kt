package com.rishikeshwadkar.socialapp.data.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.Utils
import com.rishikeshwadkar.socialapp.data.models.Chat
import kotlinx.android.synthetic.main.chat_message_item_view.view.*
import kotlinx.android.synthetic.main.fragment_chat_with_user.*

class ChatMessageAdapter(options: FirestoreRecyclerOptions<Chat>,val context: Context) :
    FirestoreRecyclerAdapter<Chat, ChatMessageAdapter.ChatMessageViewHolder>(options) {

    private var mItemCounter = 0

    class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sendingBox: TextView = itemView.sending_text_box
        val sendingTime: TextView = itemView.sending_text_time

        val receivingBox: TextView = itemView.receiving_text_box
        val receivingTime: TextView = itemView.receiving_text_time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val viewHolder = ChatMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_message_item_view, parent, false))

        return viewHolder
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int, model: Chat) {

        val mActivity = context as Activity

        if (model.from == Firebase.auth.currentUser!!.uid){
            holder.sendingBox.visibility = View.VISIBLE
            holder.sendingTime.visibility = View.VISIBLE

            holder.receivingBox.visibility = View.GONE
            holder.receivingTime.visibility = View.GONE

            holder.sendingBox.text = model.message
            holder.sendingTime.text = Utils.getTimeAgo(model.msgTime)

            goDown(mActivity)
        }
        else{
            holder.receivingBox.visibility = View.VISIBLE
            holder.receivingTime.visibility = View.VISIBLE

            holder.sendingBox.visibility = View.GONE
            holder.sendingTime.visibility = View.GONE

            holder.receivingBox.text = model.message
            holder.receivingTime.text = Utils.getTimeAgo(model.msgTime)

            goDown(mActivity)
        }
    }

    fun goDown(activity: Activity){
        Log.d("scrollChat", this.itemCount.toString())
        if (this.itemCount != mItemCounter){
            activity.chat_with_user_recycler_view.smoothScrollToPosition(this.itemCount-1)
            mItemCounter = this.itemCount
        }
    }
}