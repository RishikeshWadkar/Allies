package com.rishikeshwadkar.socialapp.fragments.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.IOException


class EditProfileFragment : Fragment() {

    val userDao: UserDao = UserDao()
    val currentUser = Firebase.auth.currentUser
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var imageBitmap: Bitmap? = null
    private var flag = false
    private val mViewModel: MyViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
        edit_proflle_save_btn.setOnClickListener {
            updateData(view)
        }
        edit_profile_changeProfilePhoto_text.setOnClickListener {
            chooseImage()
        }
    }

    private fun setData() {
        // setting up the user image and password
        val mThis = this
        CoroutineScope(Dispatchers.IO).launch {
            val user: User = userDao.getUserById(currentUser!!.uid).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main){
                if (mThis.isVisible){
                    Glide.with(edit_profile_user_image).load(user.userImage).circleCrop().into(edit_profile_user_image)
                    edit_profile_password_text.setText(user.userPassword)
                    if(user.userPhoneNo != null)
                        edit_profile_phone_text.setText(user.userPhoneNo)
                    else
                        edit_profile_phone_text.requestFocus()
                }
            }
        }
        edit_profile_name_text.setText(currentUser!!.displayName)
        edit_profile_email_text.setText(currentUser.email)
        edit_profile_name.clearFocus()
        edit_profile_name_text.clearFocus()
    }

    private fun updateData(view: View) {
        if(edit_profile_name_text.text?.isEmpty() == true){
            edit_profile_name.boxStrokeColor = Color.RED
            edit_profile_name_text.requestFocus()
            if (edit_profile_fragment_constraint_layout != null){
                Snackbar.make(edit_profile_fragment_constraint_layout, "Name should not be empty...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
        else if(edit_profile_password_text.text?.isEmpty() == true || edit_profile_password_text.text!!.length < 8){
            edit_profile_password.boxStrokeColor = Color.RED
            edit_profile_password_text.requestFocus()
        }
        else if(edit_profile_phone_text.text?.isNotEmpty() == true && edit_profile_phone_text.text!!.length < 10){
            edit_profile_phone.boxStrokeColor = Color.RED
            edit_profile_phone_text.requestFocus()
        }
        else{
            mViewModel.showDialog(requireContext(), "Updating your data")
            // password
            currentUser!!.updatePassword(edit_profile_password_text.text.toString())
            userDao.updatePassword(currentUser.uid, edit_profile_password_text.text.toString())

            // userName
            val req = UserProfileChangeRequest
                    .Builder().setDisplayName(edit_profile_name_text.text.toString()).build()
            currentUser.updateProfile(req)
            userDao.updateName(currentUser.uid, edit_profile_name_text.text.toString())

            // phone
            userDao.updatePhone(currentUser.uid, edit_profile_phone_text.text.toString())

            if (flag){
                mViewModel.uploadImageToFirebase(imageBitmap!!, edit_profile_fragment_constraint_layout, requireContext())
            }
            else{
                mViewModel.dismissDialog()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if (edit_profile_fragment_constraint_layout != null) {
                        Snackbar.make(
                            edit_profile_fragment_constraint_layout,
                            "Profile Updated",
                            Snackbar.LENGTH_LONG
                        )
                            .setIcon(
                                getDrawable(requireContext(), R.drawable.ic_check)!!,
                                resources.getColor(
                                    R.color.white
                                )
                            ).show()
                    }
                }
                Navigation.findNavController(view).navigate(R.id.action_editProfileFragment_to_myProfileFragment)
            }
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, filePath)
                edit_profile_user_image.setImageBitmap(bitmap)
                imageBitmap = bitmap
                flag = true
            } catch (e: IOException) {
                e.printStackTrace()
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