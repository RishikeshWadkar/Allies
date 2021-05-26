package com.rishikeshwadkar.socialapp.data.dao

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.data.adapter.ChatMessageAdapter
import com.rishikeshwadkar.socialapp.data.models.Chat
import com.rishikeshwadkar.socialapp.data.models.ChatCreator
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.fragment_chat_with_user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatDao {

    private val db = FirebaseFirestore.getInstance()
    val chatMetadataCollection = db.collection("chat")
    val userDao = UserDao()
    
    fun initializeChat(iFromUid: String, iToUid: String, chatCreator: ChatCreator, chat: Chat, context: Context){
        chatMetadataCollection.document("$iFromUid + $iToUid").set(chatCreator)
        chatMetadataCollection.document("$iFromUid + $iToUid").collection("messages")
            .document().set(chat)
                .addOnSuccessListener {
                    val activity: Activity = context as Activity
                    activity.chat_with_user_msg_text.text = null
                }

    }

    fun getWholeChatByBothID(iFromUid: String, iToUid: String): Task<DocumentSnapshot>{
        return chatMetadataCollection.document("$iFromUid + $iToUid").get()
    }

    fun getChatCreatorById(chatCreatorId: String): Task<DocumentSnapshot>{
        return chatMetadataCollection.document(chatCreatorId).get()
    }

    fun sendMsg(iFromUid: String, iToUid: String, chatCreator: ChatCreator, chat: Chat, adapter: ChatMessageAdapter, context: Context){

        Log.d("Chatting", "iFrom = $iFromUid iTo = $iToUid")
        Log.d("Chatting", chatCreator.toString())

        chatMetadataCollection.document("$iFromUid + $iToUid")
            .collection("messages").document().set(chat)
            .addOnSuccessListener {
                val activity: Activity = context as Activity
                activity.chat_with_user_recycler_view.smoothScrollToPosition(adapter.itemCount - 1)
                activity.chat_with_user_msg_text.text = null
            }
        chatMetadataCollection.document("$iFromUid + $iToUid").update("latestChat", chat)
    }

    fun updateSeen(iFromUid: String, iToUid: String){
        chatMetadataCollection.document("$iFromUid + $iToUid").update("latestChat.seen", true)
    }

}