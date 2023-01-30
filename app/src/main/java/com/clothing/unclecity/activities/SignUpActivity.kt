package com.clothing.unclecity.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.clothing.unclecity.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.clothing.unclecity.models.User
import com.clothing.unclecity.databinding.ActivitySignUpBinding
import com.clothing.unclecity.utils.Extensions.shortToast
import com.clothing.unclecity.utils.LoadingDialog
import com.clothing.unclecity.utils.UserType

class SignUpActivity : AppCompatActivity() {

    private val userCollectionRef = Firebase.firestore.collection("Users")

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var loadingDialog: LoadingDialog
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        loadingDialog = LoadingDialog(this)

        textAutoCheck()

        binding.isAdmin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding.verificationCode.visibility = View.VISIBLE
            } else {
                binding.verificationCode.visibility = View.GONE
            }
        }

        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.signUpButton.setOnClickListener {
            checkInputSignUp()
        }
    }

    private fun textAutoCheck() {

        binding.names.editText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (binding.names.editText!!.text.isEmpty()){
                    binding.names.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                }
                else if (binding.names.editText!!.text.length >= 4){
                    binding.names.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {

                binding.names.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (count >= 4){
                    binding.names.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }
        })

        binding.email.editText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (binding.email.editText!!.text.isEmpty()){
                    binding.email.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                }
                else if (binding.email.editText!!.text.matches(emailPattern.toRegex())) {
                    binding.email.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {

                binding.email.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (binding.email.editText!!.text.matches(emailPattern.toRegex())) {
                    binding.email.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }
        })
        binding.number.editText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (binding.number.editText!!.text.isEmpty()){
                    binding.number.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                }
                else if (binding.number.editText!!.text.matches("0[0-9]{9}".toRegex())) {
                    binding.number.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {

                binding.number.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (binding.number.editText!!.text.matches("0[0-9]{9}".toRegex())) {
                    binding.number.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }
        })

        binding.password.editText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (binding.password.editText!!.text.isEmpty()){
                    binding.password.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                }
                else if (binding.password.editText!!.text.length > 5){
                    binding.password.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {

                binding.password.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (count > 5){
                    binding.password.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }
        })

        binding.passwordConfirm.editText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (binding.passwordConfirm.editText!!.text.isEmpty()){
                    binding.passwordConfirm.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                }
                else if (binding.passwordConfirm.editText!!.text.toString() == binding.password.editText!!.text.toString()){
                    binding.passwordConfirm.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {

                binding.passwordConfirm.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (binding.passwordConfirm.editText!!.text.toString() == binding.password.editText!!.text.toString()){
                    binding.passwordConfirm.editText!!.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(applicationContext,
                        R.drawable.ic_check
                    ), null)
                }
            }
        })

    }

    private fun checkInputSignUp() {
        if (binding.names.editText!!.text.isEmpty()){
            shortToast("Name can't empty!")
            return
        }
        if (binding.email.editText!!.text.isEmpty()){
            shortToast("Email can't be empty!")
            return
        }
        if (!binding.email.editText!!.text.matches(emailPattern.toRegex())) {
            shortToast("Enter Valid Email")
            return
        }

        if (binding.number.editText!!.text.isEmpty()){
            shortToast("Phone Number can't be empty!")
            return
        }
        if (!binding.number.editText!!.text.matches("0[0-9]{9}".toRegex())) {
            shortToast("Enter Phone Number")
            return
        }

        if(binding.password.editText!!.text.isEmpty()){
            shortToast("Password can't empty!")
            return
        }
        if (binding.password.editText!!.text.toString() != binding.passwordConfirm.editText!!.text.toString()){
            shortToast("Password not Match")
            return
        }

        signUp()
    }

    private fun signUp() {
        loadingDialog.show("Creating Account...")

        val email: String = binding.email.editText!!.text.toString().trim()
        val number: String = binding.number.editText!!.text.toString().trim()
        val password: String = binding.password.editText!!.text.toString()
        val fullNames : String = binding.names.editText!!.text.toString().trim()

        if (binding.isAdmin.isChecked) {
            Firebase.firestore.collection("Store")
                .document("StoreInfo")
                .get()
                .addOnSuccessListener {
                    if (it.getString("verificationCode") == binding.verificationCode.editText!!.text.toString()) {
                        Firebase.auth.createUserWithEmailAndPassword(email,password)
                            .addOnSuccessListener { u ->
                                loadingDialog.setText("Saving User Data...")
                                val user = User(u.user?.uid!!, fullNames, email, number,UserType.ADMIN)

                                storeUserData(user)
                            }
                            .addOnFailureListener { error ->
                                loadingDialog.isError("Signing up failed.\n" +
                                        "${error.message}")
                            }
                    } else {
                        loadingDialog.isError("Incorrect Code")
                    }
                }
        } else {
            Firebase.auth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    loadingDialog.setText("Saving User Data...")
                    val user = User(it.user?.uid!!, fullNames, email, number,UserType.CUSTOMER)

                    storeUserData(user)
                }
                .addOnFailureListener {
                    loadingDialog.isError("Signing up failed.\n${it.message}")
                }
        }
    }

    var trials = 0
    private fun storeUserData(user: User) {
        userCollectionRef
            .document(Firebase.auth.currentUser?.uid!!)
            .set(user)
            .addOnSuccessListener {
                getSharedPreferences("User", Context.MODE_PRIVATE).edit()
                    .putBoolean("isAdmin",user.userType == UserType.ADMIN)
                    .apply()

                loadingDialog.isDone {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtra("isAdmin", user.userType == UserType.ADMIN)
                    })
                    finishAffinity()
                }
            }
            .addOnFailureListener {
                trials++
                if (trials <= 3)
                    storeUserData(user)
                else {
                    loadingDialog.isError("Signing up failed.\n${it.message}")
                    Firebase.auth.currentUser!!.delete()
                }
            }
    }
}


