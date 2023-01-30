package com.clothing.unclecity.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.clothing.unclecity.adapters.CartAdapter
import com.clothing.unclecity.adapters.ProductListAdapter
import com.clothing.unclecity.databinding.ActivityCartBinding
import com.clothing.unclecity.models.OrderProduct
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.utils.Extensions.to2DecimalString
import com.clothing.unclecity.utils.LoadingDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var loadingDialog : LoadingDialog

    private var orderProducts = mutableListOf<OrderProduct>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        loadingDialog = LoadingDialog(this)

        val query : Query = Firebase.firestore.collection("Products")
            .whereArrayContains("cartsUid", Firebase.auth.currentUser!!.uid)

        val productAdapter = CartAdapter( this, query)
        productAdapter.startListening()

        binding.productsRecycler.layoutManager = LinearLayoutManager(this)
        binding.productsRecycler.adapter = productAdapter

        query.addSnapshotListener { value, error ->
            if (error != null){
                return@addSnapshotListener
            }
            if (value?.isEmpty != false) {
                binding.cartBottomLayout.visibility = View.GONE
                binding.emptyLayout.visibility = View.VISIBLE
                binding.emptyEnim.playAnimation()
            } else {
                binding.emptyLayout.visibility = View.GONE
                binding.cartBottomLayout.visibility = View.VISIBLE
            }

            updateUi(value?.documents)
        }

        binding.checkOutButton.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java).apply {
                putExtra("OrderProduct", orderProducts.toTypedArray())
                putExtra("totalPrice", totalPrice)
            })
        }

    }

    private var totalPrice = 0f
    fun updateUi(snapshots: MutableList<DocumentSnapshot>?){

        var totalPrice = 0f

        val temp = mutableListOf<OrderProduct>()

        snapshots?.forEach {
            val product = it.toObject<Product>()!!

            val pref = getSharedPreferences("product_${product.code}", Context.MODE_PRIVATE)
            val selectedQuantity = pref.getInt("selected_quantity", 1)
            val selectedColor = product.colors?.get(pref.getInt("selected_color", 0)) ?: ""
            val selectedSize = product.sizes?.get(pref.getInt("selected_size", 0)) ?: ""

            val price = if (product.discounted != null && product.discounted != 0f) {
                product.discounted * selectedQuantity
            } else {
                product.price * selectedQuantity
            }
            totalPrice += price
            binding.totalProductsPrice.text = "R ${totalPrice.to2DecimalString()}"

            temp.add(
                OrderProduct(
                    product.code,
                    product.name,
                    product.brand,
                    product.imageUrl,
                    price,
                    selectedSize,
                    selectedColor,
                    selectedQuantity
                )
            )
        }
        orderProducts = temp
        this.totalPrice = totalPrice
    }
}