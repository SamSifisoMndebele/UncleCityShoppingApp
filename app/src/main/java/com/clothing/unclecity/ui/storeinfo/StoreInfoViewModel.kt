package com.clothing.unclecity.ui.storeinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clothing.unclecity.models.StoreInfo
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class StoreInfoViewModel : ViewModel() {
    private val storeRef = Firebase.firestore.collection("Store").document("StoreInfo")
    init {
        storeRef.get().addOnSuccessListener {
            val storeInfo = it?.toObject<StoreInfo>()?:StoreInfo()
            _shippingFee.value = storeInfo.shippingFee
            _businessEmail.value = storeInfo.businessEmail
            _businessNumber.value = storeInfo.businessNumber
            _queryFormLink.value = storeInfo.queryFormLink
            _verificationCode.value = storeInfo.verificationCode
            _bankDetails.value = storeInfo.bankDetails
        }
    }

    private val _shippingFee = MutableLiveData<Float>()
    private val _businessEmail = MutableLiveData<String>()
    private val _businessNumber = MutableLiveData<String>()
    private val _queryFormLink = MutableLiveData<String>()
    private val _verificationCode = MutableLiveData<String>()
    private val _bankDetails = MutableLiveData<String>()

    val shippingFee: LiveData<Float> = _shippingFee
    val businessEmail: LiveData<String> = _businessEmail
    val businessNumber: LiveData<String> = _businessNumber
    val queryFormLink: LiveData<String> = _queryFormLink
    val verificationCode: LiveData<String> = _verificationCode
    val bankDetails: LiveData<String> = _bankDetails

    fun setShippingFee(shippingFee: Float) {
        storeRef.set(mapOf("shippingFee" to shippingFee), SetOptions.merge())
    }
    fun setBusinessEmail(businessEmail: String) {
        storeRef.set(mapOf("businessEmail" to businessEmail), SetOptions.merge())
    }
    fun setBusinessNumber(businessNumber: String) {
        storeRef.set(mapOf("businessNumber" to businessNumber), SetOptions.merge())
    }
    fun setQueryFormLink(queryFormLink: String) {
        storeRef.set(mapOf("queryFormLink" to queryFormLink), SetOptions.merge())
    }
    fun setVerificationCode(verificationCode: String) {
        storeRef.set(mapOf("verificationCode" to verificationCode), SetOptions.merge())
    }
    fun setBankDetails(bankDetails: String) {
        storeRef.set(mapOf("bankDetails" to bankDetails), SetOptions.merge())
    }
}