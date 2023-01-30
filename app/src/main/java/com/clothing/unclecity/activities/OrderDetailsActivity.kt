package com.clothing.unclecity.activities

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.clothing.unclecity.adapters.CheckoutAdapter
import com.clothing.unclecity.adapters.OrderDetailsAdapter
import com.clothing.unclecity.databinding.ActivityOrderDetailsBinding
import com.clothing.unclecity.models.Order
import com.clothing.unclecity.models.OrderProduct
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.models.StoreInfo
import com.clothing.unclecity.utils.OrderStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        val order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable("Order", Order::class.java)!!
        } else {
            intent.extras?.getParcelable("Order")!!
        }
        val isAdmin = getSharedPreferences("User", Context.MODE_PRIVATE).getBoolean("isAdmin", false)


        val productAdapter = OrderDetailsAdapter( this, order.orderProducts)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = productAdapter

        val timestamp = Timestamp.now()

        var items = 0
        order.orderProducts.forEach {
            items += it.quantity
        }

        binding.shippingDetailsLayout.names.text = order.userName
        binding.shippingDetailsLayout.number.text = order.userPhone
        binding.shippingDetailsLayout.shippingAddress.text = order.deliveryAddress
        binding.shippingDetailsLayout.orderNumber.text = order.orderNumber
        binding.paymentDetailsLayout.priceItemsCount.text = if (items == 1) "Item" else "Items($items)"
        binding.shippingDetailsLayout.shipDateValueTv.text = timestamp.toDate().toString().take(16)
        binding.shippingDetailsLayout.orderStatus.text = order.orderStatus

        when(order.orderStatus){
            OrderStatus.AWAITING_PAYMENT -> {
                binding.shippingDetailsLayout.refStatusLabelTv.visibility = View.VISIBLE
                binding.shippingDetailsLayout.paymentRef.visibility = View.VISIBLE
                binding.shippingDetailsLayout.paymentRef.text = order.orderNumber
            }
            else -> {
                binding.shippingDetailsLayout.refStatusLabelTv.visibility = View.GONE
                binding.shippingDetailsLayout.paymentRef.visibility = View.GONE
            }
        }


        if (isAdmin){
            binding.shippingDetailsLayout.orderStatus.setOnClickListener {
                AlertDialog.Builder(this).apply {
                    setTitle("Change Status to")
                    setItems(OrderStatus.array) { _, item ->
                        Firebase.firestore.collection("Orders")
                            .document(order.orderNumber)
                            .update("orderStatus",OrderStatus.array[item] )
                            .addOnSuccessListener {
                                binding.shippingDetailsLayout.orderStatus.text = OrderStatus.array[item]
                            }
                    }
                    show()
                }
            }
        }


        val itemsPrice = order.itemsPrice
        val shippingFee = order.deliveryPrice
        val totalPrice = order.totalPrice

        binding.paymentDetailsLayout.shippingPrice.text = "R $shippingFee"
        binding.paymentDetailsLayout.itemsPrice.text = "R $itemsPrice"
        binding.paymentDetailsLayout.totalPrice.text = "R $totalPrice"


        Firebase.firestore.collection("Store")
            .document("StoreInfo")
            .get()
            .addOnSuccessListener {
                val storeInfo = it.toObject<StoreInfo>()
                binding.paymentDetailsLayout.bankDetails.text = storeInfo?.bankDetails
            }
    }
}