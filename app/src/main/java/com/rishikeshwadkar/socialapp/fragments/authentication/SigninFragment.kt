package com.rishikeshwadkar.socialapp.fragments.authentication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_signin.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class SigninFragment : Fragment() {

    private lateinit var googleSignInClient : GoogleSignInClient
    private var RC_SIGN_IN = 1
    private lateinit var auth: FirebaseAuth
    private val mViewModel: MyViewModel by viewModels()
    private val userDao: UserDao  = UserDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("609379926313-1etgacmlcgb5qgm8qcff0iil6ul6cub9.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        auth = Firebase.auth

        sign_in_forgot_password.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_signinFragment_to_forgotPasswordFragment)
        }

        sign_in_back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        sign_in_login_button.setOnClickListener {
            performSignIN()
        }

        sign_in_google_login.setOnClickListener {
            mViewModel.showDialog(requireContext(),"Authenticating with Google")
            signIn()
        }

        sign_in_signup_textview.setOnClickListener{
            Navigation.findNavController(view).navigate(R.id.action_signinFragment_to_signUpFragment)
        }
    }

    //==============================================================================================

    private fun performSignIN(){

        val email = sign_in_email_address_text.text
        val password = sign_in_password_text.text

        if(email!!.isEmpty()){
            sign_in_email_address.requestFocus()
            sign_in_email_address.helperText = "*Required"
            if (sign_in_constraint_layout != null) {
                Snackbar.make(
                    sign_in_constraint_layout,
                    "Enter your email...",
                    Snackbar.LENGTH_SHORT
                )
                    .setAction("Action", null).show()
            }
        }
        else if(password!!.isEmpty()){
            sign_in_password.requestFocus()
            sign_in_password.helperText = "*Required"
            if (sign_in_constraint_layout != null) {
                Snackbar.make(
                    sign_in_constraint_layout,
                    "Enter your password...",
                    Snackbar.LENGTH_SHORT
                )
                    .setAction("Action", null).show()
            }
        }
        else{
            mViewModel.showDialog(
                requireContext(),
                "Signing in"
            )

            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email.toString(),
                password.toString()
            )
                    .addOnSuccessListener {
                        Log.d(
                            "userSignIn",
                            "SignInWithEmailAndPass   email: $email password: $password"
                        )
                        mViewModel.dismissDialog()
                        mViewModel.updateUI(it.user, requireContext())
            }
                .addOnFailureListener {
                    Log.d("signinLog", it.message.toString())
                    it.message?.let { it1 -> Log.d("main", it1)
                        var msg: String = it1
                        if(it1 == "The email address is badly formatted."){
                            mViewModel.dismissDialog()
                            sign_in_email_address_text.requestFocus()
                            msg = "Please enter a valid email address..."
                            if (sign_in_constraint_layout != null) {
                                val snackbar = Snackbar.make(
                                    sign_in_constraint_layout, msg,
                                    Snackbar.LENGTH_LONG
                                ).setAction("Action", null)
                                snackbar.setTextColor(Color.WHITE)
                                snackbar.show()
                            }
                        }
                        else if (it1 == "There is no user record corresponding to this identifier. The user may have been deleted."){
                            mViewModel.dismissDialog()
                            msg = "New user account please Sign up..."
                            if (sign_in_constraint_layout != null) {
                                val snackbar = Snackbar.make(
                                    sign_in_constraint_layout, msg,
                                    Snackbar.LENGTH_LONG
                                ).setAction("Sign up") {
                                    Navigation.findNavController(requireView())
                                        .navigate(R.id.action_signinFragment_to_signUpFragment)
                                }
                                snackbar.setTextColor(Color.WHITE)
                                snackbar.setActionTextColor(Color.parseColor("#FFBB86FC"))
                                snackbar.show()
                            }
                        }
                        else if (it1 == "The password is invalid or the user does not have a password."){
                            mViewModel.dismissDialog()
                            msg = "Incorrect Password..."
                            if (sign_in_constraint_layout != null) {
                                val snackbar = Snackbar.make(
                                    sign_in_constraint_layout, msg,
                                    Snackbar.LENGTH_LONG
                                ).setAction("action", null)
                                snackbar.setTextColor(Color.WHITE)
                                snackbar.show()
                            }
                        }
                    }
                }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.i("myTag", "inside requested code")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            Log.i("myTag", "inside handlesignin")
            val account = task?.getResult(ApiException::class.java)!!
            Log.d("signINwithGoogle", "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w("signINwithGoogle", "Google sign in failed", e)
            mViewModel.dismissDialog()
            Snackbar.make(sign_in_constraint_layout, "Something went wrong!", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null)
                    .show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Log.i("myTag", "FirebaseAuthWithGoogle")

        GlobalScope.launch(Dispatchers.IO){
            auth.signInWithCredential(credential).await()
            val user = userDao.getUserById(auth.currentUser!!.uid).await().toObject(User::class.java)
            withContext(Dispatchers.Main){
                if(user == null){
                    mViewModel.dismissDialog()
                    Navigation.findNavController(requireView()).navigate(R.id.action_signinFragment_to_setupPassword)
                }
                else if(user.userPassword == ""){
                    mViewModel.dismissDialog()
                    Navigation.findNavController(requireView()).navigate(R.id.action_signinFragment_to_setupPassword)
                }
                else{
                    mViewModel.dismissDialog()
                    mViewModel.updateUI(auth.currentUser, requireContext())
                }
            }
        }
    }
}