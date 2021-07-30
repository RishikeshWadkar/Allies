package com.rishikeshwadkar.socialapp.fragments.authentication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.R
import com.rishikeshwadkar.socialapp.data.dao.UserDao
import com.rishikeshwadkar.socialapp.data.models.User
import com.rishikeshwadkar.socialapp.data.viewmodels.MyViewModel
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.fragment_setup_password.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {

    private lateinit var googleSignInClient : GoogleSignInClient
    private var RC_SIGN_IN = 1
    private var TAG: String = "SignInActivity TAG"
    private lateinit var auth: FirebaseAuth
    private val mViewModel: MyViewModel by viewModels()
    val userDao = UserDao()
    var user: com.rishikeshwadkar.socialapp.data.models.User? = com.rishikeshwadkar.socialapp.data.models.User()
    private var currentUser = Firebase.auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
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

        sign_up_back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        sign_up_google_login.setOnClickListener {
            Log.i("myTag","Button Clicked")
            mViewModel.showDialog(requireContext(), "Authenticating with Google")
            signIn()
        }

        sign_up_button.setOnClickListener {
            loginUsingEmailAndPassword()
        }

        sign_up_sign_in_textview.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_signinFragment)
        }
    }

    //==============================================================================================

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun loginUsingEmailAndPassword() {
        val name = sign_up_name_text.text
        val emailID = sign_up_email_text.text
        val phoneNo = sign_up_phone_text.text
        val pass = sign_up_password_text.text

        mViewModel.showDialog(requireContext(), "Signing Up..")

        if(name!!.isNotEmpty()){
            sign_up_name.helperText = ""
            if(emailID!!.isNotEmpty()){
                sign_up_email.helperText = ""
                    if(pass!!.isNotEmpty()){
                        if(pass.count() < 8){
                            if (signUpConstraintLayout != null){
                                Snackbar.make(signUpConstraintLayout, "Password should be at least 8 Characters...", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                            sign_up_password.requestFocus()
                            mViewModel.dismissDialog()
                            return
                        }

                        if(phoneNo.toString().isNotEmpty() && phoneNo?.count() != 10){
                            if (signUpConstraintLayout != null){
                                Snackbar.make(signUpConstraintLayout, "Enter a valid phone No.", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                            sign_up_phone.requestFocus()
                            mViewModel.dismissDialog()
                            return
                        }
                        sign_up_password.helperText = ""
                        FirebaseAuth.getInstance().
                        createUserWithEmailAndPassword(emailID.toString(), pass.toString())
                                .addOnCompleteListener {
                                    if(it.isSuccessful){
                                        Log.d("main", it.result?.user!!.uid)
                                        mViewModel.dismissDialog()
                                        mViewModel.updateUIEmail(it.result?.user!!.uid,name.toString(),emailID.toString(),phoneNo.toString(),pass.toString(),"", requireContext())
                                    }
                                    else{
                                        mViewModel.dismissDialog()
                                        it.exception?.message?.let { it1 -> Log.d("main", it1)
                                            var msg: String = it1
                                            if(it1 == "The email address is already in use by another account."){
                                                msg = "email id already exists"
                                                if (signUpConstraintLayout != null) {
                                                    val snackbar = Snackbar.make(
                                                        signUpConstraintLayout, msg,
                                                        Snackbar.LENGTH_LONG
                                                    ).setAction("Sign in") {
                                                        Navigation.findNavController(requireView())
                                                            .navigate(R.id.action_signUpFragment_to_signinFragment)
                                                    }
                                                    snackbar.setTextColor(Color.WHITE)
                                                    snackbar.setActionTextColor(Color.parseColor("#FFBB86FC"))
                                                    snackbar.show()
                                                }

                                            }
                                            else if(it1 == "The email address is badly formatted."){
                                                sign_up_email_text.requestFocus()
                                                msg = "Please enter a valid email address..."
                                                if (signUpConstraintLayout != null) {
                                                    val snackbar = Snackbar.make(
                                                        signUpConstraintLayout, msg,
                                                        Snackbar.LENGTH_LONG
                                                    ).setAction("Action", null)
                                                    snackbar.setTextColor(Color.WHITE)
                                                    snackbar.show()
                                                }
                                            }
                                        }
                                    }
                                }
                    }else{
                        mViewModel.dismissDialog()
                        if (signUpConstraintLayout != null) {
                            Snackbar.make(
                                signUpConstraintLayout,
                                "Enter your password...",
                                Snackbar.LENGTH_SHORT
                            )
                                .setAction("Action", null).show()
                            sign_up_password.helperText = "*Required"
                            sign_up_password_text.requestFocus()
                        }
                    }
            } else{
                mViewModel.dismissDialog()
                if (signUpConstraintLayout != null) {
                    Snackbar.make(
                        signUpConstraintLayout,
                        "Enter your email...",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction("Action", null).show()
                    sign_up_email.helperText = "*Required"
                    sign_up_email_text.requestFocus()
                }
            }
        } else{
            mViewModel.dismissDialog()
            if (signUpConstraintLayout != null) {
                Snackbar.make(signUpConstraintLayout, "Enter your name...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                sign_up_name.helperText = "*Required"
                sign_up_name_text.requestFocus()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.i("myTag","inside requested code")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            Log.i("myTag","inside handlesignin")
            val account = task?.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
            mViewModel.dismissDialog()
            Snackbar.make(signUpConstraintLayout, "Something went wrong!", Snackbar.LENGTH_SHORT)
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
                    Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_setupPassword)
                }
                else if(user.userPassword == ""){
                    mViewModel.dismissDialog()
                    Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_setupPassword)
                }
                else{
                    mViewModel.dismissDialog()
                    mViewModel.updateUI(auth.currentUser, requireContext())
                }
            }
        }
    }
}


