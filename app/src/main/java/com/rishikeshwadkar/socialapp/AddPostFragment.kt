package com.rishikeshwadkar.socialapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.TypedArrayUtils.getText
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.rishikeshwadkar.socialapp.dao.PostDao
import kotlinx.android.synthetic.main.fragment_add_post.*

class AddPostFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        val postDao = PostDao()
        val input = addPostTextInputText.text

        submitPostbtn.setOnClickListener {
            if(input!!.isNotEmpty()){
                Log.i("AddPost", "Post Added")
                Log.i("AddPost", input.toString())
                postDao.addPost(input.toString())
                navigation.navigate(R.id.action_addPostFragment_to_postsFragment)
            }
            else
                Log.i("AddPost", "Post not added")
        }
    }
}