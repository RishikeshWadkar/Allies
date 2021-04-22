package com.rishikeshwadkar.socialapp

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditProfileFragment : Fragment() {

    val userDao: UserDao = UserDao()
    val currentUser = Firebase.auth.currentUser

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
    }

    private fun setData() {
        // setting up the user image and password
        GlobalScope.launch(Dispatchers.IO) {
            val user: User = userDao.getUserById(currentUser!!.uid).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main){
                Glide.with(edit_profile_user_image).load(user.userImage).circleCrop().into(edit_profile_user_image)
                edit_profile_password_text.setText(user.userPassword)
                if(user.userPhoneNo != null)
                    edit_profile_phone_text.setText(user.userPhoneNo)
                else
                    edit_profile_phone_text.requestFocus()
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
            // password
            currentUser!!.updatePassword(edit_profile_password_text.text.toString())
            userDao.updatePassword(currentUser.uid,edit_profile_password_text.text.toString())

            // userName
            val req = UserProfileChangeRequest
                    .Builder().setDisplayName(edit_profile_name_text.text.toString()).build()
            currentUser.updateProfile(req)
            userDao.updateName(currentUser.uid, edit_profile_name_text.text.toString())

            // phone
            userDao.updatePhone(currentUser.uid, edit_profile_phone_text.text.toString())


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Snackbar.make(edit_profile_fragment_constraint_layout, "Profile Updated", Snackbar.LENGTH_LONG)
                        .setIcon(getDrawable(requireContext(), R.drawable.ic_check)!!, resources.getColor(R.color.white)).show()
            }
            Navigation.findNavController(view).navigate(R.id.action_editProfileFragment_to_myProfileFragment)

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