package com.rishikeshwadkar.socialapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import kotlinx.android.synthetic.main.fragment_add_post.*

class AddPostFragment : Fragment() {

    private val userDao: UserDao = UserDao()
    private val postDao = PostDao()
    private val currentUser = Firebase.auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navigation = Navigation.findNavController(view)
        val input = addPostTextInputText.text

        submitPostbtn.setOnClickListener {
            if(input!!.isNotEmpty()){
                Log.i("AddPost", "Post Added")
                Log.i("AddPost", input.toString())
                postDao.addPost(input.toString())
                userDao.updatePostCount(currentUser!!.uid)
                navigation.navigate(R.id.action_addPostFragment_to_postsFragment)
            }
            else
                Log.i("AddPost", "Post not added")
        }
    }
}