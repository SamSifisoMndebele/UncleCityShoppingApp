package com.clothing.unclecity.models

import android.os.Parcel
import android.os.Parcelable

data class StoreInfo(
    val shippingFee : Float = 0f,
    val businessEmail : String = "",
    val businessNumber: String = "",
    val queryFormLink : String = "",
    val verificationCode : String = "",
    val bankDetails: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(shippingFee)
        parcel.writeString(businessEmail)
        parcel.writeString(businessNumber)
        parcel.writeString(queryFormLink)
        parcel.writeString(verificationCode)
        parcel.writeString(bankDetails)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StoreInfo> {
        override fun createFromParcel(parcel: Parcel): StoreInfo {
            return StoreInfo(parcel)
        }

        override fun newArray(size: Int): Array<StoreInfo?> {
            return arrayOfNulls(size)
        }
    }
}