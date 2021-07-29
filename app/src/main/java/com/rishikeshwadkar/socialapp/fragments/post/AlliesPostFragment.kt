package com.rishikeshwadkar.socialapp.fragments.post

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.fragment_allies_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AlliesPostFragment : Fragment(), PostAdapter.IPostAdapter {

    lateinit var adapter: PostAdapter
    private val postDao = PostDao()
    private val userDao: UserDao = UserDao()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_allies_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        val query = postDao.postCollection.whereArrayContains("createdBy.userAllies", Firebase.auth.currentUser!!.uid)
            .orderBy("currentTime", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
        adapter = PostAdapter(options, this)

        allies_post_recycler_view.adapter = adapter
        allies_post_recycler_view.layoutManager  = LinearLayoutManager(requireContext())
        adapter.startListening()
    }

    override fun likeButtonListener(postID: String, position: Int) {
        postDao.updateLike(postID, adapter, position)
    }

    override fun userImageClickListener(postID: String) {
        val navController = Navigation.findNavController(requireView())
        postDao.goToUserByPostId(postID, navController, "allies")
    }
}