package com.clothing.unclecity.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clothing.unclecity.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProfileViewModel : ViewModel() {
    private val userRef = Firebase.firestore.collection("Users")
        .document(Firebase.auth.currentUser!!.uid)
    init {
        userRef.get().addOnSuccessListener {
            val user = it?.toObject<User>()
            _imageUrl.value = user?.imageUrl
            _email.value = user?.email
            _name2.value = user?.name
            _name.value = user?.name
            _phone.value = user?.phone
        }
    }

    private val _imageUrl = MutableLiveData<String?>()
    private val _email = MutableLiveData<String>()
    private val _name2 = MutableLiveData<String>()
    private val _name = MutableLiveData<String>()
    private val _phone = MutableLiveData<String>()

    val imageUrl: LiveData<String?> = _imageUrl
    val email: LiveData<String> = _email
    val name: LiveData<String> = _name
    val name2: LiveData<String> = _name2
    val phone: LiveData<String> = _phone

    fun setName(name: String) {
        _name2.value = name
        userRef.set(mapOf("name" to name), SetOptions.merge())
    }
    fun setPhone(phone: String) {
        userRef.set(mapOf("phone" to phone), SetOptions.merge())
    }
    fun setImage(imageUri: Uri?) {
        val userProfilePictureRef =  Firebase.storage.getReference("Users")
            .child(Firebase.auth.currentUser!!.uid)
            .child("profile_picture.png")

        if (imageUri == null){
            userProfilePictureRef.delete()
            userRef.set(mapOf("imageUrl" to null), SetOptions.merge())
        } else {
            userProfilePictureRef.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    userProfilePictureRef.downloadUrl
                }
                .addOnSuccessListener {
                    _imageUrl.value = it.toString()
                    userRef.set(mapOf("imageUrl" to it.toString()), SetOptions.merge())
                }
        }
    }
}