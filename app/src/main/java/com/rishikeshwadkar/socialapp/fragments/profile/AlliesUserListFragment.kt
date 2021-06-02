package com.rishikeshwadkar.socialapp.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.AlliesUserListAdapter
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_allies_user_list.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class AlliesUserListFragment : Fragment(), AlliesUserListAdapter.AlliesUserListInterface {

    val userDao = UserDao()
    lateinit var adapter: AlliesUserListAdapter
    val mViewModel: MyViewModel by viewModels()
    private val mNavArgs: AlliesUserListFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_allies_user_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val query = userDao.userCollection.whereArrayContains("userAllies", mNavArgs.uid)
        val options = FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java).build()

        adapter = AlliesUserListAdapter(options, this)
        allies_users_list_recyclerView.adapter = adapter
        allies_users_list_recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter.startListening()
    }

    override fun onRemoveBtnClickListener(uid: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val model: User = userDao.getUserById(uid).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main){
                when {
                    model.uid == Firebase.auth.currentUser!!.uid -> {

                    }
                    model.userAllies.contains(Firebase.auth.currentUser!!.uid) -> {
                        mViewModel.setupMaterialDialogRemoveAllies(
                            requireContext(),
                            uid,
                            Firebase.auth.currentUser!!.uid,
                            null,
                            0
                        )
                    }
                    model.userRequests.contains(Firebase.auth.currentUser!!.uid) -> {
                        mViewModel.setUpMaterialDialogRemoveRequest(
                            requireContext(),
                            Firebase.auth.currentUser!!.uid,
                            uid,
                            null,
                            0,
                            "Remove?",
                            "Are you sure?",
                            "Remove",
                            "Removing"
                        )
                    }
                    model.userRequestSent.contains(Firebase.auth.currentUser!!.uid) -> {
                        userDao.addToAllies(Firebase.auth.currentUser!!.uid, uid)
                    }
                    else -> {
                        userDao.addRequest(Firebase.auth.currentUser!!.uid, uid,null,0)
                    }
                }
            }
        }
    }

    override fun onUserClickListener(uid: String) {
        if (uid == Firebase.auth.currentUser!!.uid){
            Navigation.findNavController(requireView()).navigate(R.id.action_alliesUserListFragment_to_myProfileFragment)
        }else {
            val action = AlliesUserListFragmentDirections.actionAlliesUserListFragmentToUserProfileFragment(uid)
            Navigation.findNavController(requireView()).navigate(action)
        }
    }
}