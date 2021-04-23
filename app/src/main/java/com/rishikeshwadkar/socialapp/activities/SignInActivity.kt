package com.rishikeshwadkar.socialapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    lateinit var navHost: NavHost
    val mViewModel: MyViewModel by viewModels()
    val userDao = UserDao()
    var user: com.rishikeshwadkar.socialapp.data.models.User? = com.rishikeshwadkar.socialapp.data.models.User()
    private var currentUser = Firebase.auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        navHost = supportFragmentManager.findFragmentById(R.id.sign_in_navHost) as NavHost
        val navController: NavController = navHost.navController
    }

    override fun onStart() {
        super.onStart()

        currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            Log.d("userVerify", "currentUser is not null")
            GlobalScope.launch(Dispatchers.IO) {
                user = userDao.getUserById(currentUser!!.uid).await().toObject(com.rishikeshwadkar.socialapp.data.models.User::class.java)
                Log.d("userVerify", "entered coroutine")
                withContext(Dispatchers.Main){
                    if(user == null){
                        Log.d("userVerify", "mokla $user")

                    }
                    else if(user!!.userPassword.isNotEmpty()){
                        Log.d("userVerify", user!!.userPassword)
                        updateUI()
                    }
                    else
                        Log.d("userVerify", "$user")
                }
            }

        }else
            Log.d("userVerify", "currentUser is null")
    }

    private fun updateUI(){

        mViewModel.updateUI(currentUser, this)
    }

}