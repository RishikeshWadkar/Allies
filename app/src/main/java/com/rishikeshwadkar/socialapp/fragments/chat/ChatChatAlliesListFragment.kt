package com.rishikeshwadkar.socialapp.fragments.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.ChatChatUsersListAdapter
import com.rishikeshwadkar.socialapp.data.dao.ChatDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.ChatCreator
import kotlinx.android.synthetic.main.fragment_chat_chat_allies_list.*

class ChatChatAlliesListFragment : Fragment(), ChatChatUsersListAdapter.UserClickInterface {

    var adapter: ChatChatUsersListAdapter? = null
    private val chatDao = ChatDao()
    private val userDao = UserDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_chat_allies_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val query = chatDao.chatMetadataCollection.whereArrayContains("usersUid", Firebase.auth.currentUser!!.uid)
                .orderBy("latestChat.msgTime", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<ChatCreator>().setQuery(query, ChatCreator::class.java).build()
        adapter = ChatChatUsersListAdapter(options, this)
        chat_chat_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        chat_chat_recycler_view.adapter = adapter
        adapter!!.startListening()
    }

    override fun onStart() {
        super.onStart()
        setUpRecyclerView()
    }

    override fun onUserClickListener(chatCreatorUid: String) {
        val action = ChatViewPagerFragmentDirections.actionChatViewPagerFragmentToChatWithUserFragment(chatCreatorUid)
        Navigation.findNavController(requireView()).navigate(action)
    }
}