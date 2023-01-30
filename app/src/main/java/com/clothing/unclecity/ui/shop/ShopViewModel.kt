package com.clothing.unclecity.ui.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clothing.unclecity.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ShopViewModel : ViewModel() {
    private val _filter = MutableLiveData<String>().apply { value = "" }
    val filter: LiveData<String> = _filter
    fun setFilter(filter : String?){
        if (filter == null) _filter.value = ""
        else _filter.value = filter
    }
}