package com.rishikeshwadkar.socialapp.data.viewmodels

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.ProgressBar
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.MainActivity
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MyViewModel(application: Application): AndroidViewModel(application) {

    private val userDao: UserDao = UserDao()
    val currentUser = Firebase.auth.currentUser
    lateinit var dialog: ProgressDialog

    fun updateUI(firebaseUser: FirebaseUser?, context: Context): Boolean {
        if(firebaseUser != null){
            Log.i("myTag", "firebase user != NULL")

            val activity = context as Activity

            val user = User(firebaseUser.uid,
                    firebaseUser.displayName.toString(),
                    firebaseUser.photoUrl.toString(),
                    firebaseUser.email.toString(),
                    firebaseUser.phoneNumber.toString())

            GlobalScope.launch(Dispatchers.IO) {
                val userBool = userDao.getUserById(user.uid).await().toObject(User::class.java)
                withContext(Dispatchers.Main){
                    if(userBool == null){
                        Log.d("user", "inside if ${user.userDisplayName}")
                        userDao.addUser(user)
                    }
                    else
                        Log.d("user", "else ${user.userDisplayName}")
                }
            }

            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            activity.finish()
        }
        else{
            return false
        }
        return true
    }

    fun updateUIEmail(uid: String, name: String, email: String, phoneNO: String, pass: String, imageUrl: String, context: Context) {

        val activity = context as Activity

        Log.i("myTag", "firebase user != NULL")
        var user: User = User()
        if(imageUrl == ""){
            val imageUrl2 = "https://user-images.githubusercontent.com/59508176/114025201-83464600-9892-11eb-8856-5523f2f6de91.jpg"
            user = User(uid, name, imageUrl2, email, phoneNO, pass)
        }
        else{
            user = User(uid, name, imageUrl, email, phoneNO, pass)
        }

        userDao.addUser(user)

        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        activity.finish()

    }

    fun showDialog(context: Context){
        dialog = ProgressDialog(context)
        dialog.setMessage("Authenticating with Google")
        dialog.setCancelable(false)
        dialog.show()
        val progressbar = dialog.findViewById<ProgressBar>(android.R.id.progress)
        progressbar.indeterminateDrawable.setColorFilter(Color.parseColor("#2A00D7"), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    fun dismissDialog(){
        dialog.dismiss()
    }
}