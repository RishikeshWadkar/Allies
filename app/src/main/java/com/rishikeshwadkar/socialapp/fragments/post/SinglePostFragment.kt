package com.rishikeshwadkar.socialapp.fragments.post

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.Utils
import com.rishikeshwadkar.socialapp.data.dao.PostDao
import com.rishikeshwadkar.socialapp.data.models.Post
import kotlinx.android.synthetic.main.fragment_single_post.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SinglePostFragment : Fragment() {

    private val mNavArgs: SinglePostFragmentArgs by navArgs()
    private val postDao = PostDao()
    private var liked: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpData()

        single_post_liked_btn.setOnClickListener {
            if (liked){
                single_post_liked_btn.setImageResource(R.drawable.ic_unliked)
                postDao.updateLike(mNavArgs.postId, null, 0)
                Log.d("likebtnchange", "inside if")
                single_post_like_count.text = (single_post_like_count.text.toString().toInt() - 1).toString()
                liked = false
            }
            else{
                single_post_liked_btn.setImageResource(R.drawable.ic_liked)
                postDao.updateLike(mNavArgs.postId, null, 0)
                Log.d("likebtnchange", "inside else")
                single_post_like_count.text = (single_post_like_count.text.toString().toInt() + 1).toString()
                liked = true
            }
        }
    }

    private fun setUpData() {
        CoroutineScope(Dispatchers.IO).launch {
            val post: Post = postDao.getPostByID(mNavArgs.postId).await()
                .toObject(Post::class.java)!!

            withContext(Dispatchers.Main){
                single_post_user_name?.text = post.createdBy.userDisplayName
                single_post_created_at?.text = Utils.getTimeAgo(post.currentTime)
                single_post_post_text?.text = post.text
                single_post_like_count?.text = post.likedBy.size.toString()

                // Profile photo
                Glide.with(single_post_user_image).load(post.createdBy.userImage).circleCrop().into(single_post_user_image)

                // Like btn
                if (post.likedBy.contains(Firebase.auth.currentUser!!.uid)){
                    single_post_liked_btn.setImageResource(R.drawable.ic_liked)
                    liked = true
                }
                else{
                    single_post_liked_btn.setImageResource(R.drawable.ic_unliked)
                    liked = false
                }
            }
        }
    }
}