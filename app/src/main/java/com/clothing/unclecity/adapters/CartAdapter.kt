package com.clothing.unclecity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clothing.unclecity.activities.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.clothing.unclecity.R
import com.clothing.unclecity.activities.CartActivity
import com.clothing.unclecity.activities.ProductDetailsActivity
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.utils.Extensions.tempDisable
import com.clothing.unclecity.utils.Extensions.to2DecimalString
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class CartAdapter(
    private val activity: CartActivity,
    query: Query
) : FireStoreAdapter<CartAdapter.ViewHolder>(query) {

    private var snapshotsFiltered: MutableList<DocumentSnapshot> = getSnapshots()

    private val productsRef = Firebase.firestore.collection("Products")
    private val userId = Firebase.auth.currentUser!!.uid

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val productImage: ImageView = itemView.findViewById(R.id.product_image_view)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productBrand: TextView = itemView.findViewById(R.id.product_brand)
        private val favProduct: CheckBox = itemView.findViewById(R.id.fav_product)
        private val productDelete: ImageView = itemView.findViewById(R.id.product_delete)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productColor: TextView = itemView.findViewById(R.id.product_color)
        private val productSize: TextView = itemView.findViewById(R.id.product_size)

        private val productMinus: ImageButton = itemView.findViewById(R.id.product_minus)
        private val productQuantity: TextView = itemView.findViewById(R.id.product_quantity)
        private val productPlus: ImageButton = itemView.findViewById(R.id.product_plus)

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            val pref = activity.getSharedPreferences("product_${product.code}", Context.MODE_PRIVATE)
            var quantity = pref.getInt("selected_quantity", 1)
            productQuantity.text = quantity.toString()

            activity.updateUi(getSnapshots())

            if (product.discounted != null && product.discounted != 0f) {
                productPrice.text = "R ${(product.discounted * quantity).to2DecimalString()}"
            } else {
                productPrice.text = "R ${(product.price * quantity).to2DecimalString()}"
            }

            productPlus.setOnClickListener {
                quantity++
                pref.edit().putInt("selected_quantity", quantity).apply()
                productQuantity.text = quantity.toString()

                if (product.discounted != null && product.discounted != 0f) {
                    productPrice.text = "R ${(product.discounted * quantity).to2DecimalString()}"
                } else {
                    productPrice.text = "R ${(product.price * quantity).to2DecimalString()}"
                }

                activity.updateUi(getSnapshots())
            }
            productMinus.setOnClickListener {
                if (quantity == 1) return@setOnClickListener
                quantity--
                pref.edit().putInt("selected_quantity", quantity).apply()
                productQuantity.text = quantity.toString()

                if (product.discounted != null && product.discounted != 0f) {
                    productPrice.text = "R ${(product.discounted * quantity).to2DecimalString()}"
                } else {
                    productPrice.text = "R ${(product.price * quantity).to2DecimalString()}"
                }

                activity.updateUi(getSnapshots())
            }

            val selectedColor = product.colors?.get(pref.getInt("selected_color", 0)) ?: ""
            val selectedSize = product.sizes?.get(pref.getInt("selected_size", 0)) ?: ""
            productColor.text = "Color: $selectedColor"
            productSize.text = "Size: $selectedSize"


            favProduct.isChecked = product.favoritesUid?.contains(userId) == true
            favProduct.setOnClickListener {
                it.tempDisable()
                if (favProduct.isChecked){
                    productsRef.document(product.code)
                        .update("favoritesUid", FieldValue.arrayUnion(userId))
                } else {
                    productsRef.document(product.code)
                        .update("favoritesUid", FieldValue.arrayRemove(userId))
                }
            }
            productDelete.setOnClickListener {
                it.tempDisable()
                productsRef.document(product.code)
                    .update("cartsUid", FieldValue.arrayRemove(userId))
            }

            Glide.with(activity)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(productImage)

            productName.text = product.name
            productBrand.text = product.brand

            itemView.setOnClickListener {
                it.tempDisable()
                activity.startActivity(Intent(activity, ProductDetailsActivity::class.java).apply {
                    putExtra("ProductCode", product.code)
                    putExtra("fromCart", true)
                })
                activity.finish()
            }

            itemView.setOnLongClickListener {
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_cart_product, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (snapshotsFiltered.size > position){
            holder.bind(snapshotsFiltered[position].toObject(Product::class.java)!!)
        }
    }

    override fun getItemCount(): Int {
        return snapshotsFiltered.size
    }
}