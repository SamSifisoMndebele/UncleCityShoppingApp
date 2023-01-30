package com.clothing.unclecity.models

import android.os.Parcel
import android.os.Parcelable

data class Product (
    val code : String = "",
    val name: String = "",
    val brand: String = "",
    val price: Float = 0f,
    val discounted: Float? = null,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val sizes: List<String>? = null,
    val colors: List<String>? = null,
    val rating: Float = 0f,
    val favoritesUid: List<String>? = null,
    val cartsUid: List<String>? = null,
    val orderedCount: Long = 0,
    val quantity: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readFloat(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readLong(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeString(name)
        parcel.writeString(brand)
        parcel.writeFloat(price)
        parcel.writeValue(discounted)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
        parcel.writeString(category)
        parcel.writeStringList(sizes)
        parcel.writeStringList(colors)
        parcel.writeFloat(rating)
        parcel.writeStringList(favoritesUid)
        parcel.writeStringList(cartsUid)
        parcel.writeLong(orderedCount)
        parcel.writeLong(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}