package com.rishikeshwadkar.socialapp.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.Navigation
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
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

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
        my_profile_recycler_view.setHasFixedSize(true)
        setupRecyclerView()

        my_profile_edit_profile_btn.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_myProfileFragment_to_editProfileFragment)
        }

        my_profile_allies_number_tv.setOnClickListener {
            val action = MyProfileFragmentDirections.actionMyProfileFragmentToAlliesUserListFragment(Firebase.auth.currentUser!!.uid)
            Navigation.findNavController(view).navigate(action)
        }
        my_profile_allies_tv.setOnClickListener {
            val action = MyProfileFragmentDirections.actionMyProfileFragmentToAlliesUserListFragment(Firebase.auth.currentUser!!.uid)
            Navigation.findNavController(view).navigate(action)
        }
    }

    private fun setupRecyclerView(){
        val mThis = this

        CoroutineScope(Dispatchers.IO).launch {
            currentUser = userDao.getUserById(Firebase.auth.currentUser!!.uid).await().toObject(User::class.java)!!

            withContext(Dispatchers.Main) {
                if (mThis.isVisible){
                    Glide.with(my_profile_image_view).load(currentUser.userImage).circleCrop().into(my_profile_image_view)
                    val query: Query = postDao.postCollection
                        .whereEqualTo("createdBy.uid", Firebase.auth.currentUser!!.uid)
                        .orderBy("currentTime", Query.Direction.DESCENDING)

                    val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
                    //Log.d("mQuery", recyclerViewOptions.snapshots.getSnapshot(0).id)
                    adapter = PostAdapter(recyclerViewOptions, mThis)
                    adapter!!.startListening()
                    my_profile_recycler_view?.adapter = adapter
                    my_profile_recycler_view?.layoutManager = LinearLayoutManager(context)

                    // Recycler View Done
                    my_profile_user_name?.text = currentUser.userDisplayName
                    val postsCount: Int = currentUser.userPostCount
                    my_profile_post_number_tv?.text = postsCount.toString()
                    my_profile_allies_number_tv?.text = currentUser.userAllies.size.toString()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
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