package com.clothing.unclecity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isEmpty
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.clothing.unclecity.models.Category
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.ui.profile.ProfileViewModel
import com.clothing.unclecity.ui.shop.ShopViewModel
import com.clothing.unclecity.utils.Extensions.tempDisable
import com.clothing.unclecity.utils.Extensions.to2DecimalString
import com.clothing.unclecity.utils.LoadingDialog
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.collections.ArrayList

class CategoriesListAdapter(
    private val activity: MainActivity,
    query: Query
) : FireStoreAdapter<CategoriesListAdapter.ViewHolder>(query), Filterable {

    private var snapshotsFiltered: MutableList<DocumentSnapshot> = getSnapshots()
    private val shopViewModel = ViewModelProvider(activity)[ShopViewModel::class.java]

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.name)
        private val description: TextView = itemView.findViewById(R.id.description)
        private val productsRecycler: RecyclerView = itemView.findViewById(R.id.products_recycler)

        @SuppressLint("SetTextI18n")
        fun bind(snapshot: DocumentSnapshot) {
            val category = snapshot.toObject(Category::class.java)!!

            name.text = category.name
            description.text = category.description

            val query : Query = Firebase.firestore.collection("Products")
                .whereEqualTo("category", category.name)

            query.addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                if (value?.isEmpty == true){
                    Firebase.firestore.collection("Categories")
                        .document(category.name)
                        .delete()
                }
            }

            val productAdapter = ProductListAdapter( activity, query,name)
            productAdapter.startListening()

            productsRecycler.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            productsRecycler.setHasFixedSize(true)
            productsRecycler.adapter = productAdapter

            shopViewModel.filter.observe(activity){
                productAdapter.filter.filter(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_category, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (snapshotsFiltered.size > position){
            holder.bind(snapshotsFiltered[position])
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
                        val product = snapshot.toObject(Category::class.java)!!
                        if (product.name.lowercase().contains(pattern) ||
                            product.description?.lowercase()?.contains(pattern) == true) {
                            filteredList.add(snapshot)
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
            }
        }
    }
}