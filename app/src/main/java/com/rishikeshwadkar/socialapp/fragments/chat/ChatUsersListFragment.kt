package com.rishikeshwadkar.socialapp.fragments.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.ChatUsersListAdapter
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.fragment_chat_users_list.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class ChatUsersListFragment : Fragment(), ChatUsersListAdapter.OnUserClicked {

    var adapter: ChatUsersListAdapter? = null
    private var usersDao = UserDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_users_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView(){
        val mThis = this
        CoroutineScope(Dispatchers.IO).launch() {
            val currUser = usersDao.getUserById(Firebase.auth.currentUser!!.uid).await().toObject(User::class.java)!!
            var userArrayList: ArrayList<User> = ArrayList()

            for (allies in currUser.userAllies){
                val user = usersDao.getUserById(allies).await().toObject(User::class.java)!!
                Log.d("chatting", "in loop ${user.userDisplayName}")
                userArrayList.add(user)
            }
            withContext(Dispatchers.Main){
                if(userArrayList.isNotEmpty() && mThis.isVisible){
                    Log.d("chatting", "not null ${userArrayList[0]}")
                    adapter = ChatUsersListAdapter(userArrayList, mThis)
                    chat_allies_users_recycler_view.adapter = adapter
                    chat_allies_users_recycler_view.layoutManager = LinearLayoutManager(requireContext())
                }
                else
                    Log.d("chatting", "array is null")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setUpRecyclerView()
    }

    override fun onUserClicked(uid: String) {
        val actions = ChatViewPagerFragmentDirections.actionChatViewPagerFragmentToChatWithUserFragment(uid)
        Navigation.findNavController(requireView()).navigate(actions)
    }

}