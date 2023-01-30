package com.clothing.unclecity.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.clothing.unclecity.databinding.ActivityLoginBinding
import com.clothing.unclecity.models.User
import com.clothing.unclecity.utils.Extensions.shortToast
import com.clothing.unclecity.utils.LoadingDialog
import com.clothing.unclecity.utils.UserType

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        loadingDialog = LoadingDialog(this)
        
        binding.signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            checkInputSignIn()
        }

        binding.loginForgotTv.setOnClickListener {
            startActivity(Intent(this, PasswordActivity::class.java))
        }
    }

    private fun checkInputSignIn() {

        if (binding.email.editText!!.text.isEmpty()){
            shortToast("Email can't be empty")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.editText!!.text).matches()) {
            shortToast("Invalid email")
            return
        }

        if(binding.password.editText!!.text.isEmpty()){
            shortToast("Password Can't be Empty")
            return
        }

        signInUser()
    }


    private fun signInUser() {
        loadingDialog.show("Signing in...")
        val email = binding.email.editText!!.text.toString().trim()
        val password = binding.password.editText!!.text.toString()

        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                loadingDialog.setText("Fetching user info...")
                Firebase.firestore.collection("Users")
                    .document(it.user!!.uid)
                    .get()
                    .addOnSuccessListener { value->
                        val user = value?.toObject<User>()
                        getSharedPreferences("User", Context.MODE_PRIVATE).edit()
                            .putBoolean("isAdmin", user?.userType == UserType.ADMIN)
                            .apply()

                        loadingDialog.isDone {
                            startActivity(Intent(this, MainActivity::class.java).apply {
                                putExtra("isAdmin", user?.userType == UserType.ADMIN)
                            })
                            finishAffinity()
                        }
                    }
                    .addOnFailureListener { err ->
                        loadingDialog.isError("Signing in failed\n${err.message}")
                        Firebase.auth.signOut()
                    }
            }
            .addOnFailureListener {
                loadingDialog.isError("Signing in failed\n${it.message}")
            }
        }
}