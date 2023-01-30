package com.clothing.unclecity.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.clothing.unclecity.utils.OrderStatus

data class Order (
    val orderNumber : String = "",
    val userUid:String = "",
    val userName: String = "",
    val userPhone: String = "",
    val deliveryAddress: String = "",
    val orderProducts: List<OrderProduct> = listOf(),
    val deliveryPrice : Float = 0f,
    val itemsPrice : Float = 0f,
    val totalPrice : Float = 0f,
    val timestamp: Timestamp = Timestamp.now(),
    val orderStatus : String = OrderStatus.AWAITING_PAYMENT
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(OrderProduct)!!,
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readParcelable(Timestamp::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(orderNumber)
        parcel.writeString(userUid)
        parcel.writeString(userName)
        parcel.writeString(userPhone)
        parcel.writeString(deliveryAddress)
        parcel.writeTypedList(orderProducts)
        parcel.writeFloat(deliveryPrice)
        parcel.writeFloat(itemsPrice)
        parcel.writeFloat(totalPrice)
        parcel.writeParcelable(timestamp, flags)
        parcel.writeString(orderStatus)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Order> {
        override fun createFromParcel(parcel: Parcel): Order {
            return Order(parcel)
        }

        override fun newArray(size: Int): Array<Order?> {
            return arrayOfNulls(size)
        }
    }
}