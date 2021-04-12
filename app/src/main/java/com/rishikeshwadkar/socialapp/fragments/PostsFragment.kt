package com.rishikeshwadkar.socialapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.adapter.PostAdapter
import com.rishikeshwadkar.socialapp.data.models.Post
import kotlinx.android.synthetic.main.fragment_posts.*

class PostsFragment : Fragment(), PostAdapter.IPostAdapter {

    private lateinit var adapter: PostAdapter
    private lateinit var postDao: PostDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floatingActionButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_postsFragment_to_addPostFragment)
        }
        setUpRecyclerView()
        val db = FirebaseFirestore.getInstance()
    }

    private fun setUpRecyclerView() {
        postDao = PostDao()
        val postCollection = postDao.postCollection
        val query = postCollection.orderBy("currentTime", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
        adapter = PostAdapter(recyclerViewOptions,this)

        postRecyclerView.adapter = adapter
        postRecyclerView.layoutManager = LinearLayoutManager(context)

        val dividerItemDecoration = DividerItemDecoration(postRecyclerView.context,
                (postRecyclerView.layoutManager as LinearLayoutManager).orientation)
        postRecyclerView.addItemDecoration(dividerItemDecoration)

//        val currentUser = Firebase.auth.currentUser
//        Log.d("pass", "pass -> ${currentUser!!.displayName}")
//        if(currentUser!!.displayName == "R1SH1"){
//            currentUser.updatePassword("12345678")
//            Log.d("pass", "updated!!!!")
//        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun likeButtonListener(postID: String) {
        postDao.updateLike(postID)
    }
}