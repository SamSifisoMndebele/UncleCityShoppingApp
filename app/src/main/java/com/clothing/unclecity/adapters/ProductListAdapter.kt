package com.clothing.unclecity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clothing.unclecity.activities.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.clothing.unclecity.R
import com.clothing.unclecity.activities.AddProductActivity
import com.clothing.unclecity.activities.EditProductActivity
import com.clothing.unclecity.activities.ProductDetailsActivity
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.utils.Extensions.tempDisable
import com.clothing.unclecity.utils.Extensions.to2DecimalString
import com.clothing.unclecity.utils.LoadingDialog
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.collections.ArrayList

class ProductListAdapter(
    private val activity: MainActivity,
    query: Query,
    private val nameView: TextView
) : FireStoreAdapter<ProductListAdapter.ViewHolder>(query), Filterable {

    private var snapshotsFiltered: MutableList<DocumentSnapshot> = getSnapshots()
    private val isAdmin = activity.getSharedPreferences("User", Context.MODE_PRIVATE).getBoolean("isAdmin", false)

    private val productsRef = Firebase.firestore.collection("Products")
    private val userId = Firebase.auth.currentUser!!.uid

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val productImage: ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productBrand: TextView = itemView.findViewById(R.id.product_brand)
        private val productDesc: TextView = itemView.findViewById(R.id.product_desc)
        private val productRating: RatingBar = itemView.findViewById(R.id.product_rating)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productActualPrice: TextView = itemView.findViewById(R.id.product_actual_price)
        private val productDelete: ImageView = itemView.findViewById(R.id.product_delete)
        private val favProduct: CheckBox = itemView.findViewById(R.id.fav_product)
        private val productEdit: ImageView = itemView.findViewById(R.id.product_edit)
        private val productAddToCart: CheckBox = itemView.findViewById(R.id.product_add_cart)

        val loadingDialog = LoadingDialog(itemView.context)

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            if (isAdmin){
                favProduct.visibility = View.GONE
                productAddToCart.visibility = View.GONE
                productDelete.visibility = View.VISIBLE
                productEdit.visibility = View.VISIBLE

                productDelete.setOnClickListener {
                    AlertDialog.Builder(activity)
                        .setTitle("Delete ${product.name}?")
                        .setMessage("The product will be permanently deleted on store.")
                        .setPositiveButton("Delete"){d,_->
                            d.dismiss()
                            loadingDialog.show("Deleting product details...")
                            Firebase.firestore.collection("Products")
                                .document(product.code)
                                .delete()
                                .addOnSuccessListener {
                                    loadingDialog.show("Deleting product picture...")
                                    Firebase.storage.getReference("Products")
                                        .child("${product.code}.png")
                                        .delete()
                                        .addOnCompleteListener {
                                            loadingDialog.isDone {
                                                it.dismiss()
                                            }
                                        }
                                }
                                .addOnFailureListener { err->
                                    loadingDialog.isError(err.message)
                                }
                        }
                        .setNegativeButton("Cancel"){d,_-> d.dismiss()}
                        .create()
                        .show()
                }

                productEdit.setOnClickListener {
                    activity.startActivity(Intent(activity, EditProductActivity::class.java).apply {
                        putExtra("Product", product)
                    })
                }
            }
            else {
                favProduct.visibility = View.VISIBLE
                productAddToCart.visibility = View.VISIBLE
                productDelete.visibility = View.GONE
                productEdit.visibility = View.GONE

                favProduct.isChecked = product.favoritesUid?.contains(userId) == true
                productAddToCart.isChecked = product.cartsUid?.contains(userId) == true

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
                productAddToCart.setOnClickListener {
                    it.tempDisable()
                    if (productAddToCart.isChecked){
                        productsRef.document(product.code)
                            .update("cartsUid", FieldValue.arrayUnion(userId))
                    } else {
                        productsRef.document(product.code)
                            .update("cartsUid", FieldValue.arrayRemove(userId))
                    }
                }
            }




            Glide.with(activity)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(productImage)

            productRating.rating = product.rating
            productName.text = product.name
            productBrand.text = product.brand
            productDesc.text = product.description

            if (product.discounted != null && product.discounted != 0f) {
                productActualPrice.text = Html.fromHtml("""<strike>R ${product.price.to2DecimalString()}<\strike>""")
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
            R.layout.item_product, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        nameView.visibility = View.VISIBLE
        if (snapshotsFiltered.size > position){
            holder.bind(snapshotsFiltered[position].toObject(Product::class.java)!!)
        }
    }

    override fun getItemCount(): Int {
        return snapshotsFiltered.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val pattern = constraint.toString().lowercase(Locale.getDefault())
                snapshotsFiltered = if (pattern.isEmpty()) {
                    getSnapshots()
                } else {
                    val filteredList = arrayListOf<DocumentSnapshot>()
                    for (snapshot in getSnapshots()) {
                        val searchFilter = activity.getSharedPreferences("search_filter", Context.MODE_PRIVATE)
                            .getString("id", "action_no_filter")
                        when(searchFilter){
                            "action_no_filter" -> {
                                val product = snapshot.toObject(Product::class.java)!!
                                if (product.category.lowercase().contains(pattern) || product.brand.lowercase().contains(pattern) ||
                                    product.description.lowercase().contains(pattern) || product.name.lowercase().contains(pattern)
                                ) {
                                    filteredList.add(snapshot)
                                }
                            }
                            "action_product_name" ->{
                                val product = snapshot.toObject(Product::class.java)!!
                                if (product.name.lowercase().contains(pattern)
                                ) {
                                    filteredList.add(snapshot)
                                }
                            }
                            "action_brand" ->{
                                val product = snapshot.toObject(Product::class.java)!!
                                if (product.brand.lowercase().contains(pattern)) {
                                    filteredList.add(snapshot)
                                }
                            }
                            "action_category" ->{
                                val product = snapshot.toObject(Product::class.java)!!
                                if (product.category.lowercase().contains(pattern)) {
                                    filteredList.add(snapshot)
                                }
                            }
                            "action_description" ->{
                                val product = snapshot.toObject(Product::class.java)!!
                                if (product.description.lowercase().contains(pattern)) {
                                    filteredList.add(snapshot)
                                }
                            }
                        }
                    }

                    filteredList
                }

                return FilterResults().apply { values = snapshotsFiltered }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                snapshotsFiltered = results.values as ArrayList<DocumentSnapshot>
                notifyDataSetChanged()
                nameView.visibility = View.INVISIBLE
            }
        }
    }
}