package com.clothing.unclecity.models

import android.os.Parcel
import android.os.Parcelable

data class OrderProduct (
    val code : String = "",
    val name: String = "",
    val brand: String = "",
    val imageUrl: String = "",
    val price: Float = 0f,
    val size: String = "",
    val color: String = "",
    val quantity: Int = 1
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeString(name)
        parcel.writeString(brand)
        parcel.writeString(imageUrl)
        parcel.writeFloat(price)
        parcel.writeString(size)
        parcel.writeString(color)
        parcel.writeInt(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderProduct> {
        override fun createFromParcel(parcel: Parcel): OrderProduct {
            return OrderProduct(parcel)
        }

        override fun newArray(size: Int): Array<OrderProduct?> {
            return arrayOfNulls(size)
        }
    }
}