package com.rishikeshwadkar.socialapp

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chat_with_user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatWithUserFragment : Fragment() {

    private val mArgs: ChatWithUserFragmentArgs by navArgs()
    val userDao: UserDao = UserDao()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_with_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView2.text = mArgs.oppositeUid
        setToolBarData()
        chat_with_user_back_btn.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setToolBarData() {
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userDao.getUserById(mArgs.oppositeUid).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main){
                chat_with_user_name.text = user.userDisplayName
                Glide.with(chat_with_user_image).load(user.userImage).into(chat_with_user_image)
            }
        }
    }





}