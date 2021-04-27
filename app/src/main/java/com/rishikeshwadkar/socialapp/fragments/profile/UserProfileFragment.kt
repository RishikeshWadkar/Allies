package com.rishikeshwadkar.socialapp.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.PostAdapter
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserProfileFragment : Fragment(), PostAdapter.IPostAdapter {

    private val mNavArgs: UserProfileFragmentArgs by navArgs()
    private val userDao = UserDao()
    private val postDao: PostDao = PostDao()
    lateinit var mView: View
    private val mViewModel: MyViewModel by viewModels()
    private var adapter: PostAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        setupData()
        user_profile_add_to_allies_btn.setOnClickListener {
            // add request sent functionality
        }
    }

    private fun setupData() {
        val mThis = this
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userDao.getUserById(mNavArgs.uid).await().toObject(User::class.java)!!
            val query: Query = postDao.postCollection.whereEqualTo("createdBy.uid", user.uid)
                .orderBy("currentTime", Query.Direction.DESCENDING)
            val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()

            withContext(Dispatchers.Main){
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