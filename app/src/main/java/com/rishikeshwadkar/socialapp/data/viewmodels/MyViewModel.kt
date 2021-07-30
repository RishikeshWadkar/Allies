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
import com.rishikeshwadkar.socialapp.data.adapter.NotificationsAdapter
import com.rishikeshwadkar.socialapp.data.dao.NotificationsDao
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import com.rishikeshwadkar.socialapp.activities.SignInActivity


class MyViewModel(application: Application): AndroidViewModel(application) {

    private val userDao: UserDao = UserDao()
    val currentUser = Firebase.auth.currentUser
    lateinit var dialog: ProgressDialog
    private val notificationsDao = NotificationsDao()
    private lateinit var mDialog: MaterialDialog.Builder



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
                            AppCompatResources.getDrawable(context, R.drawable.ic_check)!!,
                            context.resources.getColor(
                                R.color.white
                            )
                        ).show()
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

    fun setupMaterialDialogRemoveAllies(
        context: Context,
        uid: String,
        likerUid: String,
        adapter: NotificationsAdapter?,
        position: Int
    ){
        mDialog = MaterialDialog.Builder(context as Activity)
        mDialog.setTitle("Remove?")
        mDialog.setMessage("Are you sure")
        mDialog.setCancelable(false)
        mDialog.setAnimation("delete_remove.json")
        mDialog.setPositiveButton(
            "Remove",
            R.drawable.add_button_shape
        ){ dialogInterface, which ->
                dialogInterface.dismiss()
                showDialog(context, "Removing")
                context.user_profile_add_to_allies_btn?.text = "Add to Allies"
                removeFromAllies(uid, likerUid, adapter, position)
            if (context.user_profile_constraint_layout != null){
                context.user_profile_msg_user.visibility = View.GONE
            }

        }.setNegativeButton(
            "Cancel", R.drawable.added_button_shape
        ) { dialogInterface, which -> dialogInterface.dismiss() }
            .build().show()
    }

    private fun removeFromAllies(
        uid: String,
        oppositeUserUid: String,
        adapter: NotificationsAdapter?,
        position: Int
    ){

        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userDao.getUserById(uid)
                .await().toObject(User::class.java)!!
            val oppositeUser: User = userDao.getUserById(oppositeUserUid)
                .await().toObject(User::class.java)!!

            user.userAllies.remove(oppositeUserUid)
            oppositeUser.userAllies.remove(uid)

            userDao.userCollection.document(uid).update("userAllies", user.userAllies)
            userDao.userCollection.document(oppositeUserUid).update(
                "userAllies",
                oppositeUser.userAllies
            )
                .addOnSuccessListener {
                    if(adapter != null){
                        notificationsDao.removeNotification(adapter.snapshots.getSnapshot(position).id)
                        dismissDialog()
                        adapter.notifyItemChanged(position)
                    }else
                        dismissDialog()
                }
        }
    }

    private fun removeRequest(fromUid: String, toUid: String, adapter: NotificationsAdapter?, position: Int){
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userDao.getUserById(fromUid).await().toObject(User::class.java)!!
            val oppositeUser = userDao.getUserById(toUid).await().toObject(User::class.java)

            user.userRequestSent.remove(toUid)
            oppositeUser!!.userRequests.remove(fromUid)
            userDao.userCollection.document(fromUid).update("userRequestSent", user.userRequestSent)
            userDao.userCollection.document(toUid).update("userRequests", oppositeUser.userRequests)
                    .addOnSuccessListener {
                        notificationsDao.notificationsCollection
                                .whereEqualTo("to", toUid)
                                .whereEqualTo("from", fromUid)
                                .whereEqualTo("type", "Request")
                                .get().addOnSuccessListener { documents ->
                                    for (document in documents){
                                        notificationsDao.notificationsCollection.document(document.id).delete()
                                    }
                                }
                    }
            withContext(Dispatchers.Main){
                adapter?.notifyItemChanged(position)
            }
        }
    }

    fun setUpMaterialDialogRemoveRequest(
            context: Context,
            fromUid: String,
            toUid: String,
            adapter: NotificationsAdapter?,
            position: Int,
            title: String,
            msg: String,
            positiveBtn: String,
            loadingText: String
    ){
        mDialog = MaterialDialog.Builder(context as Activity)
        mDialog.setTitle(title)
        mDialog.setMessage(msg)
        mDialog.setCancelable(false)
        mDialog.setAnimation("delete_remove.json")
        mDialog.setPositiveButton(
                positiveBtn,
                R.drawable.add_button_shape
        ){ dialogInterface, which ->
            CoroutineScope(Dispatchers.IO).launch {
                val user: User = userDao.getUserById(fromUid).await().toObject(User::class.java)!!
                withContext(Dispatchers.Main){
                    if (user.userAllies.contains(toUid)){
                        if (context.user_profile_msg_user != null){
                            Snackbar.make(context.user_profile_msg_user, "The other person accepted your allies request..", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                            context.user_profile_add_to_allies_btn.text = "Allies"
                            context.user_profile_msg_user.visibility = View.VISIBLE
                            dialogInterface.dismiss()
                        }
                        else{
                            dialogInterface.dismiss()
                        }
                    }
                    else{
                        dialogInterface.dismiss()
                        removeRequest(fromUid, toUid, adapter,position)
                    }
                }
            }
            //showDialog(context, loadingText)
            try {
                context.user_profile_add_to_allies_btn.text = "Add to Allies"
            }
            catch (e: NullPointerException){}
        }.setNegativeButton(
                "Cancel", R.drawable.added_button_shape
        ) { dialogInterface, which -> dialogInterface.dismiss() }
                .build().show()
    }

    fun setUpMaterialDialogCommon(
        context: Context,
        title: String,
        msg: String,
        positiveBtn: String,
        loadingText: String
    ){
        val activity = context as Activity

        mDialog = MaterialDialog.Builder(context)
        mDialog.setTitle(title)
        mDialog.setMessage(msg)
        mDialog.setCancelable(false)
        mDialog.setAnimation("log_out.json")
        mDialog.setPositiveButton(
            positiveBtn,
            R.drawable.add_button_shape
        ){ dialogInterface, which ->

            showDialog(context, loadingText)
            Firebase.auth.signOut()
            val intent = Intent(context, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            activity.finish()

        }.setNegativeButton(
            "Cancel", R.drawable.added_button_shape
        ) { dialogInterface, which -> dialogInterface.dismiss() }
            .build().show()
    }
}