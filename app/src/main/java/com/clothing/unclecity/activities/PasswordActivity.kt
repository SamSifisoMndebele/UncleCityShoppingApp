package com.clothing.unclecity.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.clothing.unclecity.databinding.ActivityPasswordBinding
import com.clothing.unclecity.models.User
import com.clothing.unclecity.utils.Extensions.longToast
import com.clothing.unclecity.utils.Extensions.shortToast
import com.clothing.unclecity.utils.LoadingDialog
import com.clothing.unclecity.utils.UserType
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class PasswordActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPasswordBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        loadingDialog = LoadingDialog(this)

        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.resetButton.setOnClickListener {
            checkInputReset()
        }
    }

    private fun checkInputReset() {

        if (binding.email.editText!!.text.isEmpty()){
            shortToast("Email can't be empty")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.editText!!.text).matches()) {
            shortToast("Invalid email")
            return
        }

        resetPassword()
    }


    private fun resetPassword() {
        loadingDialog.show()
        val email = binding.email.editText!!.text.toString().trim()

        Firebase.auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                loadingDialog.isDone("An email to reset your password is sent to the provided email.", 3000) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {
                loadingDialog.isError(it.message)
            }
    }
}