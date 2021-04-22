package com.rishikeshwadkar.socialapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.data.adapter.PostAdapter
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.Post
import com.rishikeshwadkar.socialapp.data.models.User
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        setupData()
    }

    private fun setupData() {
        val mThis = this
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userDao.getUserById(mNavArgs.uid).await().toObject(User::class.java)!!
            val query: Query = postDao.postCollection.whereEqualTo("createdBy.uid", user.uid)
            val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()

            withContext(Dispatchers.Main){
                user_profile_user_name.text = user.userDisplayName
                Glide.with(user_profile_image_view).load(user.userImage).circleCrop().into(user_profile_image_view)
                user_profile_post_number_tv.text = user.userPostCount.toString()

                val adapter = PostAdapter(recyclerViewOptions, mThis)
                user_profile_recycler_view.adapter = adapter
                user_profile_recycler_view.layoutManager = LinearLayoutManager(requireContext())
                adapter.startListening()
            }
        }
    }

    override fun likeButtonListener(postID: String) {
        postDao.updateLike(postID)
    }

    override fun userImageClickListener(postID: String) {
        postDao.getUserUidByPostId(postID, mView)
    }

}