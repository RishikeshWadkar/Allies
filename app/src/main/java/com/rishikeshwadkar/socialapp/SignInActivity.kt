package com.rishikeshwadkar.socialapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Color.red
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.internal.ViewUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rishikeshwadkar.socialapp.dao.PostDao
import com.rishikeshwadkar.socialapp.dao.UserDao
import com.rishikeshwadkar.socialapp.models.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private lateinit var googleSignInClient : GoogleSignInClient
    private var RC_SIGN_IN = 1
    private var TAG: String = "SignInActivity TAG"
    private lateinit var auth: FirebaseAuth

    private val userDao: UserDao = UserDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        googleSIgnInButton.setOnClickListener {
            Log.i("myTag","Button Clicked")
            signIn()
            progressBar.visibility = VISIBLE
            googleSIgnInButton.visibility = GONE
        }

        submitButton.setOnClickListener {
            loginUsingEmailAndPassword()
        }

    }

    private fun loginUsingEmailAndPassword() {
        val name = textInputNameEditText.text
        val emailID = emailAddressText.text
        val phoneNo = textInputPhoneText.text
        val pass = textInputPasswordText.text

        if(name!!.isNotEmpty()){
            if(emailID!!.isNotEmpty()){
                if(phoneNo!!.isNotEmpty()){
                    if(pass!!.isNotEmpty()){
                        FirebaseAuth.getInstance().
                        createUserWithEmailAndPassword(emailID.toString(), pass.toString())
                                .addOnCompleteListener {
                                    if(it.isSuccessful){
                                        Log.d("main", it.result?.user!!.uid)
                                        updateUIEmail(it.result?.user!!.uid,name.toString(),emailID.toString(),phoneNo.toString(),pass.toString())
                                    }
                                    else{
                                        it.exception?.message?.let { it1 -> Log.d("main", it1)
                                            val snackbar = Snackbar.make(constreaintLayout, it1,
                                                    Snackbar.LENGTH_LONG).setAction("Action", null)
                                            snackbar.setTextColor(Color.RED)
                                            snackbar.show()

                                        }
                                    }
                                }
                    }else{
                        textInputPasswordText.requestFocus()
                    }
                }else{
                    textInputPhoneText.requestFocus()
                }
            } else{
                emailAddressText.requestFocus()
            }
        } else{
            textInputNameEditText.requestFocus()
        }

    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
            progressBar.visibility = GONE
            googleSIgnInButton.visibility = VISIBLE
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Log.i("myTag","FirebaseAuthWithGoogle")

        GlobalScope.launch (Dispatchers.IO){
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main){
                updateUI(firebaseUser)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null){
            Log.i("myTag","firebase user != NULL")

            val user = User(firebaseUser.uid,
                    firebaseUser.displayName.toString(),
                    firebaseUser.photoUrl.toString(),"","","")

            GlobalScope.launch(Dispatchers.IO) {
                val userBool = userDao.getUserById(user.uid).await().toObject(User::class.java)

                if(userBool == null){
                    Log.d("user","inside if ${user.userDisplayName}")
                    userDao.addUser(user)
                }
                else
                    Log.d("user", "else ${user.userDisplayName}")
            }



            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            progressBar.visibility = GONE
            googleSIgnInButton.visibility = VISIBLE
        }
    }

    private fun updateUIEmail(uid: String, name: String, email: String, phoneNO: String, pass: String) {

            Log.i("myTag","firebase user != NULL")
            val imageUrl: String = "https://user-images.githubusercontent.com/59508176/114025201-83464600-9892-11eb-8856-5523f2f6de91.jpg"
            val user = User(uid,name,imageUrl,email,phoneNO,pass)
                userDao.addUser(user)

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
    }
}