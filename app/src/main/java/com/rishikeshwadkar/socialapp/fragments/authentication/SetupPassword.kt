package com.rishikeshwadkar.socialapp.fragments.authentication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.fragment_setup_password.*

class SetupPassword : Fragment() {

    val userDao: UserDao = UserDao()
    val mViewModel: MyViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setup_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = Firebase.auth.currentUser
        val password = confirm_enter_pass_text.text
        val confirmPassword = confirm_confirm_pass_text.text

        confirm_enter_pass_text.addTextChangedListener {
            if(confirm_enter_pass_text.length() < 8){
                confirm_enter_pass.boxStrokeColor = Color.RED
            }
            else{
                confirm_enter_pass.boxStrokeColor = Color.GREEN
                confirm_enter_pass.setEndIconDrawable(R.drawable.ic_check)
            }
        }
        confirm_confirm_pass_text.addTextChangedListener {
            if(confirm_enter_pass_text.text.toString() != confirm_confirm_pass_text.text.toString())
                confirm_confirm_pass.boxStrokeColor = Color.RED
            else
                confirm_confirm_pass.boxStrokeColor = Color.GREEN
        }

        confirm_submit_button.setOnClickListener {
            if( (confirm_enter_pass_text.length() < 8) || (confirm_enter_pass_text.text.toString() != confirm_confirm_pass_text.text.toString())){
                if(confirm_enter_pass.boxStrokeColor == Color.RED){
                    if (setupPasswordConstraintLayout != null){
                    Snackbar.make(setupPasswordConstraintLayout, "At least 8 Characters...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show()
                    }
                    confirm_enter_pass.requestFocus()
                }
                else{
                    if (setupPasswordConstraintLayout != null) {
                        Snackbar.make(
                            setupPasswordConstraintLayout,
                            "Passwords didn't match...",
                            Snackbar.LENGTH_SHORT
                        )
                            .setAction("Action", null).show()
                        confirm_confirm_pass.requestFocus()
                    }
                }
            }
            else{
                Log.d("confirm", "clicked")
                currentUser!!.updatePassword(password.toString())
                Log.d("confirm", currentUser.displayName)
                mViewModel.updateUIEmail(
                        currentUser.uid,
                        currentUser.displayName!!,
                        currentUser.email!!,
                        "",
                        password.toString(),
                        currentUser.photoUrl.toString(),
                        requireContext()
                )
            }

        }
    }
}