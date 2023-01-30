package com.clothing.unclecity.models

import com.clothing.unclecity.utils.UserType

data class User(
    val uid:String = "",
    val name: String = "",
    val email:String = "",
    val phone: String = "",
    val userType : Int = UserType.CUSTOMER,
    val imageUrl: String? = null
)