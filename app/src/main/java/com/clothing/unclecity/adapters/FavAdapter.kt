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
import com.clothing.unclecity.activities.FavActivity
import com.clothing.unclecity.activities.ProductDetailsActivity
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.utils.Extensions.tempDisable
import com.clothing.unclecity.utils.Extensions.to2DecimalString
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class FavAdapter(
    private val activity: FavActivity,
    query: Query
) : FireStoreAdapter<FavAdapter.ViewHolder>(query) {

    private var snapshotsFiltered: MutableList<DocumentSnapshot> = getSnapshots()

    private val productsRef = Firebase.firestore.collection("Products")
    private val userId = Firebase.auth.currentUser!!.uid

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val productImage: ImageView = itemView.findViewById(R.id.product_image_view)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productBrand: TextView = itemView.findViewById(R.id.product_brand)
        private val productDelete: ImageView = itemView.findViewById(R.id.product_delete)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val addToCart: TextView = itemView.findViewById(R.id.add_to_cart)

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            productDelete.setOnClickListener {
                it.tempDisable()
                productsRef.document(product.code)
                    .update("favoritesUid", FieldValue.arrayRemove(userId))
            }

            if (product.cartsUid?.contains(userId) == true){
                addToCart.text = "Remove from Cart"
                addToCart.setOnClickListener {
                    it.tempDisable()
                    productsRef.document(product.code)
                        .update("cartsUid", FieldValue.arrayRemove(userId))
                }
            } else {
                addToCart.text = "Add to Cart"
                addToCart.setOnClickListener {
                    it.tempDisable()
                    productsRef.document(product.code)
                        .update("cartsUid", FieldValue.arrayUnion(userId))
                }
            }

            Glide.with(activity)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(productImage)

            productName.text = product.name
            productBrand.text = product.brand

            if (product.discounted != null && product.discounted != 0f) {
                productPrice.text = "R ${product.discounted.to2DecimalString()}"
            } else {
                productPrice.text = "R ${product.price.to2DecimalString()}"
            }

            itemView.setOnClickListener {
                it.tempDisable()
                activity.startActivity(Intent(activity, ProductDetailsActivity::class.java).apply {
                    putExtra("ProductCode", product.code)
                })
            }

            itemView.setOnLongClickListener {
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_fav_product, parent, false)
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