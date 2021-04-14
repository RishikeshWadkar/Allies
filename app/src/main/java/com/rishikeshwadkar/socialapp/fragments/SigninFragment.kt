package com.rishikeshwadkar.socialapp.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
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
    //lateinit var dialog: ProgressDialog

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

        //dialog = ProgressDialog(requireContext())

        sign_in_login_button.setOnClickListener {
            performSignIN()
        }

        sign_in_google_login.setOnClickListener {
            mViewModel.showDialog(requireContext())
            //showDialog()
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
        }
        else if(password!!.isEmpty()){
            sign_in_password.requestFocus()
            sign_in_password.helperText = "*Required"
        }
        else{
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email.toString(),
                password.toString()
            )
                    .addOnSuccessListener {
                        Log.d(
                            "userSignIn",
                            "SignInWithEmailAndPass   email: $email password: $password"
                        )
                        mViewModel.updateUI(it.user, requireContext())
            }
                    .addOnFailureListener {
                        Log.d("userSignIn", "${it.message}")
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
                else
                    mViewModel.updateUI(auth.currentUser, requireContext())
            }
        }
    }
}