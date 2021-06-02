package com.rishikeshwadkar.socialapp.fragments.post

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.adapter.PostAdapter
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.models.Post
import kotlinx.android.synthetic.main.fragment_posts.*
import kotlinx.coroutines.*


class PostsFragment : Fragment(), PostAdapter.IPostAdapter {

    private lateinit var adapter: PostAdapter
    private var postDao: PostDao = PostDao()
    private lateinit var query: Query
    private val postCollection = postDao.postCollection
    lateinit var mView: View

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
    }

    private fun setUpRecyclerView() {
        val mThis = this
        CoroutineScope(Dispatchers.IO).launch {
            query = postCollection.orderBy("currentTime", Query.Direction.DESCENDING)
            val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
            adapter = PostAdapter(recyclerViewOptions, mThis)
            withContext(Dispatchers.Main){
                postRecyclerView.adapter = adapter
                postRecyclerView.layoutManager = LinearLayoutManager(context)
                adapter.startListening()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setUpRecyclerView()
    }

    override fun onStop() {
       super.onStop()
        adapter.stopListening()
    }

    override fun likeButtonListener(postID: String, position: Int) {
        postDao.updateLike(postID, adapter, position)
    }

    override fun userImageClickListener(postID: String) {
        val navController: NavController = Navigation.findNavController(requireView())
        postDao.goToUserByPostId(postID, navController, "post")
    }
}