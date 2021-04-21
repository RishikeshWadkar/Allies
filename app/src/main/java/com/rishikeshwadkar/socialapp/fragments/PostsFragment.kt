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
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostsFragment : Fragment(), PostAdapter.IPostAdapter {

    private lateinit var adapter: PostAdapter
    private var postDao: PostDao = PostDao()
    private lateinit var query: Query
    private val postCollection = postDao.postCollection

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
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val mThis = this
        GlobalScope.launch(Dispatchers.IO) {
            query = postCollection.orderBy("currentTime", Query.Direction.DESCENDING)
            val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
            withContext(Dispatchers.Main){
                adapter = PostAdapter(recyclerViewOptions,mThis)
                postRecyclerView.adapter = adapter
                postRecyclerView.layoutManager = LinearLayoutManager(context)
                adapter.startListening()
            }
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
       adapter.stopListening()
    }

    override fun likeButtonListener(postID: String) {
        postDao.updateLike(postID)
    }
}