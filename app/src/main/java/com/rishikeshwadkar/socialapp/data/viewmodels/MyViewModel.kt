package com.rishikeshwadkar.socialapp.data.viewmodels

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.Navigation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.activities.MainActivity
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class MyViewModel(application: Application): AndroidViewModel(application) {

    private val userDao: UserDao = UserDao()
    val currentUser = Firebase.auth.currentUser
    lateinit var dialog: ProgressDialog

    fun updateUI(firebaseUser: FirebaseUser?, context: Context): Boolean {
        if(firebaseUser != null){
            Log.i("myTag", "firebase user != NULL")

            val activity = context as Activity

            GlobalScope.launch(Dispatchers.IO) {
                val userBool = userDao.getUserById(firebaseUser.uid).await().toObject(User::class.java)
                withContext(Dispatchers.Main){
                    if(userBool == null){
                        Log.d("user", "inside if ${firebaseUser.uid}")
                        val user = User(
                                firebaseUser.uid,
                                firebaseUser.displayName.toString(),
                                firebaseUser.photoUrl.toString(),
                                firebaseUser.email.toString(),
                                ""
                        )
                        userDao.addUser(user)
                    }
                    else
                        Log.d("user", "else ${firebaseUser.uid}")
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

    fun updateUIEmail(
        uid: String,
        name: String,
        email: String,
        phoneNO: String,
        pass: String,
        imageUrl: String,
        context: Context
    ) {

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

    fun showDialog(context: Context, text: String){
        dialog = ProgressDialog(context)
        dialog.setMessage(text)
        dialog.setCancelable(false)
        dialog.show()
        val progressbar = dialog.findViewById<ProgressBar>(android.R.id.progress)
        progressbar.indeterminateDrawable.setColorFilter(
            Color.parseColor("#2A00D7"),
            android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

    fun dismissDialog(){
        dialog.dismiss()
    }

    fun uploadImageToFirebase(bitmap: Bitmap, view: View, context: Context){
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data: ByteArray = baos.toByteArray()

        val firebaseStorage = FirebaseStorage.getInstance()
        val uid: String = currentUser!!.uid
        val storageReference = firebaseStorage.reference
        val imageRef = storageReference.child("images/${uid}.jpg")

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            // upload failed
            Log.d("imageUpload", "failed to upload image")
        }.addOnSuccessListener {
            val result: Task<Uri> = it.metadata!!.reference!!.downloadUrl
            result.addOnSuccessListener {
                val url: String = it.toString()
                userDao.updatePhoto(currentUser.uid, url)
                Log.d("imageUpload", "successful $url")


                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Snackbar.make(view, "Profile Updated", Snackbar.LENGTH_LONG)
                        .setIcon(
                            AppCompatResources.getDrawable(context, R.drawable.ic_check)!!, context.resources.getColor(
                            R.color.white
                        )).show()
                }
                Navigation.findNavController(view).navigate(R.id.action_editProfileFragment_to_myProfileFragment)
                dismissDialog()
            }

        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun Snackbar.setIcon(drawable: Drawable, @ColorInt colorTint: Int): Snackbar {
        return this.apply {
            setAction(" ") {}
            val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
            textView.text = ""

            drawable.setTint(colorTint)
            drawable.setTintMode(PorterDuff.Mode.SRC_ATOP)
            textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
    }
}