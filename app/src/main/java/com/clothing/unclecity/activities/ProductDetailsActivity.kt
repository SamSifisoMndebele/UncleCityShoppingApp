package com.clothing.unclecity.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.clothing.unclecity.R
import com.clothing.unclecity.models.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.clothing.unclecity.databinding.ActivityProductDetailsBinding
import com.clothing.unclecity.utils.Extensions.shortToast
import com.clothing.unclecity.utils.Extensions.tempDisable
import com.clothing.unclecity.utils.Extensions.to2DecimalString
import com.clothing.unclecity.utils.LoadingDialog
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.ktx.storage

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailsBinding
    private lateinit var loadingDialog: LoadingDialog
    private var registration : ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        val productCode = intent.getStringExtra("ProductCode")!!
        val isAdmin = getSharedPreferences("User", MODE_PRIVATE).getBoolean("isAdmin", false)

        registration = Firebase.firestore.collection("Products")
            .document(productCode)
            .addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                val product = value?.toObject<Product>()
                if (product != null) {
                    showProductInfo(product, isAdmin)
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }

    }

    override fun onDestroy() {
        registration?.remove()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra("fromCart", false)){
            startActivity(Intent(this, CartActivity::class.java))
        } else super.onBackPressed()
    }

    @SuppressLint("SetTextI18n")
    private fun showProductInfo(product: Product, isAdmin : Boolean){

        if (isAdmin){
            binding.productDelete.visibility = View.VISIBLE
            binding.productFav.visibility = View.GONE

            binding.productButton.text = "Edit product details"
            binding.productButton.setOnClickListener {
                startActivity(Intent(this, EditProductActivity::class.java).apply {
                    putExtra("Product", product)
                })
            }

            binding.proDetailsSelectSizeLabel.text = Html.fromHtml("<b>Sizes</b>\n${product.sizes?.joinToString(", ")}")
            binding.proDetailsSelectColorLabel.text = Html.fromHtml("<b>Colors</b>\n${product.colors?.joinToString(", ")}")

            binding.productDelete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete the product?")
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
                                            finish()
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


            binding.productAnalytics.removeAllViews()
            binding.productAnalytics.addView(TextView(this).apply {
                text = Html.fromHtml("<b>Product Stats</b>")
                textSize = 18f
                setPadding(0,32,0,16)
            })
            binding.productAnalytics.addView(TextView(this).apply {
                text = "${product.quantity} products available in online store."
            })
            binding.productAnalytics.addView(TextView(this).apply {
                text = "The product is ordered ${product.orderedCount} times."
            })
            binding.productAnalytics.addView(TextView(this).apply {
                text = "The products is in ${product.cartsUid?.size?:0} people cart."
            })
            binding.productAnalytics.addView(TextView(this).apply {
                text = "The products is in ${product.favoritesUid?.size?:0} people favorites."
            })
        }
        else {
            binding.productDelete.visibility = View.GONE
            binding.productFav.visibility = View.VISIBLE
            binding.colorsRadioGroup.removeAllViews()
            binding.sizesRadioGroup.removeAllViews()

            val pref = getSharedPreferences("product_${product.code}", MODE_PRIVATE)
            val selectedColor = pref.getInt("selected_color", 0)
            val selectedSize = pref.getInt("selected_size", 0)

            product.colors?.forEachIndexed { index, color ->
                val radio = RadioButton(this)
                val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 16, 0)
                radio.layoutParams = params
                radio.text = color
                radio.id = index

                if (index == selectedColor) radio.isChecked = true

                binding.colorsRadioGroup.addView(radio, index)
            }
            product.sizes?.forEachIndexed { index, color ->
                val radio = RadioButton(this)
                val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 16, 0)
                radio.layoutParams = params
                radio.text = color
                radio.id = index

                if (index == selectedSize) radio.isChecked = true

                binding.sizesRadioGroup.addView(radio, index)
            }

            binding.colorsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                pref.edit().putInt("selected_color", checkedId).apply()
            }
            binding.sizesRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                pref.edit().putInt("selected_size", checkedId).apply()
            }

            binding.cartButton.visibility = View.VISIBLE
            binding.cartButton.setOnClickListener {
                startActivity(Intent(this, CartActivity::class.java))
            }

            val userId = Firebase.auth.currentUser!!.uid
            binding.productFav.isChecked = product.favoritesUid?.contains(userId) == true
            binding.productFav.setOnClickListener {
                it.tempDisable()
                if (binding.productFav.isChecked){
                     Firebase.firestore.collection("Products").document(product.code)
                        .update("favoritesUid", FieldValue.arrayUnion(userId))
                } else {
                    Firebase.firestore.collection("Products").document(product.code)
                        .update("favoritesUid", FieldValue.arrayRemove(userId))
                }
            }


            var inCart: Boolean
            if (product.cartsUid?.contains(userId) == true){
                inCart = true
                binding.productButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_cart, 0)
                binding.productButton.text = "Remove from Cart"
            } else {
                inCart = false
                binding.productButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_add_shopping_cart_24, 0)
                binding.productButton.text = "Add to Cart"
            }


            binding.productButton.setOnClickListener {
                if (inCart){
                    Firebase.firestore.collection("Products").document(product.code)
                        .update("cartsUid", FieldValue.arrayRemove(userId))
                    inCart = false
                    binding.productButton.text = "Add to Cart"
                    binding.productButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.ic_add_shopping_cart_24, 0)
                } else {
                    Firebase.firestore.collection("Products").document(product.code)
                        .update("cartsUid", FieldValue.arrayUnion(userId))
                    inCart = true
                    binding.productButton.text = "Remove from Cart"
                    binding.productButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.ic_cart, 0)
                }
            }
        }

        if (product.discounted != null && product.discounted != 0f) {
            binding.productActualPrice.text = Html.fromHtml("""<strike>R ${product.price.to2DecimalString()}<\strike>""")
            binding.productPrice.text = "R ${product.discounted.to2DecimalString()}"
        } else {
            binding.productPrice.text = "R ${product.price.to2DecimalString()}"
        }


        Glide.with(this)
            .load(product.imageUrl)
            .into(binding.imageView)

        binding.productCategory.text = product.category
        binding.productName.text = product.name
        binding.productBrand.text = product.brand
        binding.productDesc.text = product.description
        binding.productRatingBar.rating = product.rating

    }

}


