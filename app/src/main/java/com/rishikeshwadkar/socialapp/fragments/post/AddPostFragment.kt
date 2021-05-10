package com.rishikeshwadkar.socialapp.fragments.post

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_add_post.*

class AddPostFragment : Fragment() {

    private val userDao: UserDao = UserDao()
    private val postDao = PostDao()
    private val currentUser = Firebase.auth.currentUser
    private val mViewModel: MyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navigation: NavController = Navigation.findNavController(view)
        val input = addPostTextInputText.text

        submitPostbtn.setOnClickListener {
            if(input!!.isNotEmpty()){
                mViewModel.showDialog(requireContext(), "Loading...")
                Log.i("AddPost", "Post Added")
                Log.i("AddPost", input.toString())
                postDao.addPost(input.toString(), navigation, mViewModel)
                userDao.updatePostCount(currentUser!!.uid)
            }
            else
                Log.i("AddPost", "Post not added")
        }
    }
}