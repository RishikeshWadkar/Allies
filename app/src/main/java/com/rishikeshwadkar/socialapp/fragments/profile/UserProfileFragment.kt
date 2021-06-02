package com.rishikeshwadkar.socialapp.fragments.profile

import android.os.Bundle
import android.text.format.Time
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.internal.common.CurrentTimeProvider
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.PostAdapter
import com.rishikeshwadkar.socialapp.data.dao.NotificationsDao
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Notification
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class UserProfileFragment : Fragment(), PostAdapter.IPostAdapter {

    private val mNavArgs: UserProfileFragmentArgs by navArgs()
    private val userDao = UserDao()
    private val postDao: PostDao = PostDao()
    lateinit var mView: View
    private val mViewModel: MyViewModel by viewModels()
    private var adapter: PostAdapter? = null
    private val notificationsDao: NotificationsDao = NotificationsDao()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        setupData()
        setUpLiveData()

        user_profile_add_to_allies_btn.setOnClickListener {
            addToAllies()
        }

        user_profile_msg_user.setOnClickListener {
            val action = UserProfileFragmentDirections.actionUserProfileFragmentToChatWithUserFragment(mNavArgs.uid)
            Navigation.findNavController(view).navigate(action)
        }

        user_profile_allies_number_tv.setOnClickListener {
            val action = UserProfileFragmentDirections.actionUserProfileFragmentToAlliesUserListFragment(mNavArgs.uid)
            Navigation.findNavController(view).navigate(action)
        }

        user_profile_allies_tv.setOnClickListener {
            val action = UserProfileFragmentDirections.actionUserProfileFragmentToAlliesUserListFragment(mNavArgs.uid)
            Navigation.findNavController(view).navigate(action)
        }
    }

    private fun addToAllies() {
        if (user_profile_add_to_allies_btn.text == "Add to Allies"){
            userDao.addRequest(Firebase.auth.currentUser!!.uid, mNavArgs.uid,null, 0)
            CoroutineScope(Dispatchers.IO).launch {
                val user: User = userDao.getUserById(Firebase.auth.currentUser!!.uid)
                        .await().toObject(User::class.java)!!
                val currentTime: Long = System.currentTimeMillis()
                val notification: Notification = Notification(
                        "Request",
                        user.uid,
                        mNavArgs.uid,
                        "${user.userDisplayName} sent you an allies friend request",
                        currentTime,
                        ""
                )
                notificationsDao.addAddToAllieRequestsNotification(notification)
            }
            user_profile_add_to_allies_btn.text = "Request Sent"
        }
        else if (user_profile_add_to_allies_btn.text == "Request Sent"){
            mViewModel.setUpMaterialDialogRemoveRequest(
                    requireContext(),
                    Firebase.auth.currentUser!!.uid,
                    mNavArgs.uid,
                    null,
                    0,
                    "Remove Request?",
                    "Are you sure?",
                    "Remove",
                    "Removing")
        }
        else if (user_profile_add_to_allies_btn.text == "Accept"){
            userDao.addToAllies(Firebase.auth.uid!!, mNavArgs.uid)
            Log.d("removeNotification", "currUID -> ${Firebase.auth.currentUser!!.uid} oppositeUid -> $mNavArgs")
            notificationsDao.notificationsCollection.whereEqualTo("from", mNavArgs.uid)
                    .whereEqualTo("type", "Request")
                    .whereEqualTo("to", Firebase.auth.currentUser!!.uid).get().addOnSuccessListener { documents ->
                        for (document in documents){
                            Log.d("removeNotification", document.id)
                            notificationsDao.removeNotification(document.id)
                        }
                    }
            user_profile_add_to_allies_btn.text = "Allies"
        }
        else if (user_profile_add_to_allies_btn.text == "Allies"){
            mViewModel.setupMaterialDialogRemoveAllies(requireContext(),
            Firebase.auth.currentUser!!.uid,
            mNavArgs.uid,
            null,
            0)
        }
    }

    private fun setupData() {
        val mThis = this
        CoroutineScope(Dispatchers.IO).launch {
            val user: User = userDao.getUserById(mNavArgs.uid).await().toObject(User::class.java)!!
            val query: Query = postDao.postCollection.whereEqualTo("createdBy.uid", user.uid)
                .orderBy("currentTime", Query.Direction.DESCENDING)
            val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()

            withContext(Dispatchers.Main){
                if (mThis.isVisible){
                    user_profile_user_name.text = user.userDisplayName
                    Glide.with(user_profile_image_view).load(user.userImage).circleCrop().into(user_profile_image_view)
                    user_profile_post_number_tv.text = user.userPostCount.toString()

                    adapter = PostAdapter(recyclerViewOptions, mThis)
                    user_profile_recycler_view.adapter = adapter
                    user_profile_recycler_view.layoutManager = LinearLayoutManager(requireContext())
                    adapter!!.startListening()
                }
            }
        }
    }

    private fun setUpLiveData(){

        GlobalScope.launch(Dispatchers.IO) {
            val currentUser = userDao.getUserById(Firebase.auth.currentUser!!.uid).await().toObject(User::class.java)!!
            val oppositeUser = userDao.getUserById(mNavArgs.uid).await().toObject(User::class.java)!!

            withContext(Dispatchers.Main){
                when {
                    currentUser.userRequestSent.contains(oppositeUser.uid) -> {
                        user_profile_msg_user.visibility = View.GONE
                        user_profile_add_to_allies_btn.text = "Request Sent"
                    }
                    currentUser.userRequests.contains(oppositeUser.uid) -> {
                        user_profile_msg_user.visibility = View.GONE
                        user_profile_add_to_allies_btn.text = "Accept"
                    }
                    currentUser.userAllies.contains(oppositeUser.uid) -> {
                        user_profile_msg_user.visibility = View.VISIBLE
                        user_profile_add_to_allies_btn.text = "Allies"
                    }
                    else -> {user_profile_add_to_allies_btn.text = "Add to Allies"
                        user_profile_msg_user.visibility = View.GONE
                    }
                }
                user_profile_allies_number_tv.text = oppositeUser.userAllies.size.toString()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupData()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun likeButtonListener(postID: String, position: Int) {
        postDao.updateLike(postID, adapter!!, position)
    }

    override fun userImageClickListener(postID: String) {

    }

}