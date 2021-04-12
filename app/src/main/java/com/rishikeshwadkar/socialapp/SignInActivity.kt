package com.rishikeshwadkar.socialapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import androidx.navigation.NavHost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_sign_up.*

class SignInActivity : AppCompatActivity() {

    val mViewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val navHost: NavHost = supportFragmentManager.findFragmentById(R.id.sign_in_navHost) as NavHost
        val navController: NavController = navHost.navController

    }

    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        mViewModel.updateUI(currentUser,this)
    }

}