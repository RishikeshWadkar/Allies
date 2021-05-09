package com.rishikeshwadkar.socialapp.fragments.chat

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.ChatMessageAdapter
import com.rishikeshwadkar.socialapp.data.dao.ChatDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Chat
import com.rishikeshwadkar.socialapp.data.models.ChatCreator
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.fragment_chat_with_user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatWithUserFragment : Fragment() {

    private val mArgs: ChatWithUserFragmentArgs by navArgs()
    private val userDao: UserDao = UserDao()
    private val chatDao: ChatDao = ChatDao()
    private var iFrom: String = ""
    private var iTo: String = ""
    var adapter: ChatMessageAdapter? = null
    var mItemCounter = 0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_with_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolBarData()
        setUpRecyclerView()

        chat_with_user_send_btn.setOnClickListener {
            sendMsg()
        }
        chat_with_user_back_btn.setOnClickListener {
            requireActivity().onBackPressed()
        }


    }

    private fun setUpRecyclerView() {

        GlobalScope.launch(Dispatchers.IO) {

            var chatCreator: ChatCreator?
            chatCreator = chatDao.getWholeChatByBothID(
                    Firebase.auth.currentUser!!.uid,
                    mArgs.oppositeUid
            )
                .await().toObject(ChatCreator::class.java)
            if (chatCreator == null){
                chatCreator = chatDao.getWholeChatByBothID(
                        mArgs.oppositeUid,
                        Firebase.auth.currentUser!!.uid
                )
                    .await().toObject(ChatCreator::class.java)
            }

            withContext(Dispatchers.Main){
                if (chatCreator != null){
                    iFrom = chatCreator.initializedFrom
                    iTo = chatCreator.initializedTo
                    Log.d("Chatting", "$iFrom + $iTo")

                    val query: Query = chatDao.chatMetadataCollection
                        .document("$iFrom + $iTo").collection("messages").orderBy(
                                    "msgTime",
                                    Query.Direction.ASCENDING
                            )

                    val options = FirestoreRecyclerOptions.Builder<Chat>().setQuery(
                            query,
                            Chat::class.java
                    ).build()

                    adapter = ChatMessageAdapter(options, requireContext())
                    chat_with_user_recycler_view.adapter = adapter
                    val llm = LinearLayoutManager(requireContext())
                    llm.stackFromEnd = true
                    chat_with_user_recycler_view.layoutManager = llm
                    adapter!!.startListening()
                    mItemCounter = adapter!!.itemCount
                    handler.postDelayed(runnable, 1000);
                }
            }
        }
    }

    private val handler: Handler = Handler()
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            adapter?.goDown(context as Activity)
            handler.postDelayed(this, 100)
        }
    }

    private fun sendMsg() {
        if (chat_with_user_msg_text.text.isNotEmpty()){

            GlobalScope.launch(Dispatchers.IO) {
                var chatCreator: ChatCreator?
                val currentTime: Long = System.currentTimeMillis()

                chatCreator = chatDao.getWholeChatByBothID(
                        Firebase.auth.currentUser!!.uid,
                        mArgs.oppositeUid
                )
                        .await().toObject(ChatCreator::class.java)
                if (chatCreator == null){
                    chatCreator = chatDao.getWholeChatByBothID(
                            mArgs.oppositeUid,
                            Firebase.auth.currentUser!!.uid
                    )
                            .await().toObject(ChatCreator::class.java)
                }
                withContext(Dispatchers.Main){
                    if (chatCreator == null){
                        Log.d("Chatting", "not Initialized")

                        val chat: Chat = Chat(
                                Firebase.auth.currentUser!!.uid,
                                mArgs.oppositeUid,
                                chat_with_user_msg_text.text.toString(),
                                currentTime
                        )
                        chatCreator = ChatCreator(
                                Firebase.auth.currentUser!!.uid,
                                mArgs.oppositeUid
                        )

                        chatDao.initializeChat(
                                Firebase.auth.currentUser!!.uid,
                                mArgs.oppositeUid,
                                chatCreator!!,
                                chat
                        )
                        setUpRecyclerView()
                        if(adapter != null){
                            chat_with_user_recycler_view.smoothScrollToPosition(adapter!!.itemCount)
                        }
                    }
                    else{
                        iFrom = chatCreator!!.initializedFrom
                        iTo = chatCreator!!.initializedTo

                        val chat: Chat = Chat(
                                Firebase.auth.currentUser!!.uid,
                                mArgs.oppositeUid,
                                chat_with_user_msg_text.text.toString(),
                                currentTime
                        )
                        chatDao.sendMsg(
                                iFrom,
                                iTo,
                                chatCreator!!,
                                chat,
                                adapter!!,
                                requireContext()
                        )
                    }
                }
            }
        }
        else{
            chat_with_user_msg_text.requestFocus()
        }
    }

    private fun setToolBarData() {
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userDao.getUserById(mArgs.oppositeUid).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main){
                chat_with_user_name.text = user.userDisplayName
                Glide.with(chat_with_user_image).load(user.userImage).into(chat_with_user_image)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter?.stopListening()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
        adapter?.stopListening()
    }

}