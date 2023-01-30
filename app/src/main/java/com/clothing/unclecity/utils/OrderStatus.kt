package com.clothing.unclecity.utils

object OrderStatus {
    const val AWAITING_PAYMENT = "Awaiting Payment"
    const val AWAITING_SHIPPING = "Awaiting Shipping"
    const val SHIPPED = "Shipped"
    const val COMPLETED = "Completed"
    val array = arrayOf(AWAITING_PAYMENT, AWAITING_SHIPPING, SHIPPED, COMPLETED)
}


