package com.rishikeshwadkar.socialapp.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.PostAdapter
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.fragment_my_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MyProfileFragment : Fragment(), PostAdapter.IPostAdapter {

    private val postDao = PostDao()
    private val userDao = UserDao()
    private val db = FirebaseFirestore.getInstance()
    private var adapter: PostAdapter? = null
    private var currentUser: User = User()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        my_proflie_recycler_view.setHasFixedSize(true)
        setupRecyclerView()
    }

    private fun setupRecyclerView(){
        val mThis = this

        GlobalScope.launch(Dispatchers.IO) {
            currentUser = userDao.getUserById(Firebase.auth.currentUser!!.uid).await().toObject(User::class.java)!!

            withContext(Dispatchers.Main) {
                    Glide.with(my_profile_image_view).load(currentUser.userImage).circleCrop().into(my_profile_image_view)
                    val query: Query = postDao.postCollection
                            .whereEqualTo("createdBy.uid", Firebase.auth.currentUser!!.uid)

                    val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
                    //Log.d("mQuery", recyclerViewOptions.snapshots.getSnapshot(0).id)
                    adapter = PostAdapter(recyclerViewOptions, mThis)
                    adapter!!.startListening()
                    my_proflie_recycler_view.adapter = adapter
                    my_proflie_recycler_view.layoutManager = LinearLayoutManager(context)
                    // Recycler View Done
                    my_profile_user_name.text = currentUser.userDisplayName
                    val postsCount: Int = currentUser.userPostCount
                    my_profile_post_number_tv.text = postsCount.toString()
            }
        }
    }

    override fun likeButtonListener(postID: String) {
        postDao.updateLike(postID)
    }

}