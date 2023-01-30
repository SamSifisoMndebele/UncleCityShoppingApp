package com.clothing.unclecity.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.clothing.unclecity.R
import com.clothing.unclecity.adapters.CartAdapter
import com.clothing.unclecity.adapters.CheckoutAdapter
import com.clothing.unclecity.databinding.ActivityCheckoutBinding
import com.clothing.unclecity.models.Order
import com.clothing.unclecity.models.OrderProduct
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.models.StoreInfo
import com.clothing.unclecity.utils.Extensions.shortToast
import com.clothing.unclecity.utils.Extensions.to2DecimalString
import com.clothing.unclecity.utils.LoadingDialog
import com.clothing.unclecity.utils.OrderStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        val orderProducts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra("OrderProduct", OrderProduct::class.java)!!.toList()
        } else {
            val temp = mutableListOf<OrderProduct>()
            intent.getParcelableArrayExtra("OrderProduct")?.forEach {
                temp.add(it as OrderProduct)
            }
            temp.toList()
        }


        val productAdapter = CheckoutAdapter( this, orderProducts)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = productAdapter

        val timestamp = Timestamp.now()

        var items = 0
        orderProducts.forEach {
            items += it.quantity
        }

        binding.paymentDetailsLayout.priceItemsCount.text = if (items == 1) "Item" else "Items($items)"
        binding.shippingDetailsLayout.shipDateValueTv.text = timestamp.toDate().toString().take(16)
        binding.shippingDetailsLayout.orderStatus.text = OrderStatus.AWAITING_PAYMENT


        val itemsPrice = intent.getFloatExtra("totalPrice", 0f)
        var shippingFee = 0f
        var totalPrice = intent.getFloatExtra("totalPrice", 0f)

        Firebase.firestore.collection("Store")
            .document("StoreInfo")
            .get()
            .addOnSuccessListener {
                val storeInfo = it.toObject<StoreInfo>()

                shippingFee = (storeInfo?.shippingFee)?:0f
                totalPrice += shippingFee

                binding.paymentDetailsLayout.shippingPrice.text = "R $shippingFee"
                binding.paymentDetailsLayout.itemsPrice.text = "R $itemsPrice"
                binding.paymentDetailsLayout.totalPrice.text = "R $totalPrice"

                binding.paymentDetailsLayout.bankDetails.text = storeInfo?.bankDetails
            }

        binding.placeOrderButton.setOnClickListener {
            if (binding.shippingDetailsLayout.names.text.isNullOrEmpty()){
                shortToast("Enter your names")
                binding.shippingDetailsLayout.names.requestFocus()
                return@setOnClickListener
            }
            if (binding.shippingDetailsLayout.number.text.isNullOrEmpty()){
                shortToast("Enter your phone number")
                binding.shippingDetailsLayout.number.requestFocus()
                return@setOnClickListener
            }
            if (binding.shippingDetailsLayout.shippingAddress.text.isNullOrEmpty()){
                shortToast("Enter your Shipping Address")
                binding.shippingDetailsLayout.shippingAddress.requestFocus()
                return@setOnClickListener
            }
            loadingDialog.show("Placing Order...")

            val orderNumber = timestamp.seconds.toString()
            val order = Order(
                orderNumber,
                Firebase.auth.currentUser!!.uid,
                binding.shippingDetailsLayout.names.text.toString().trim(),
                binding.shippingDetailsLayout.number.text.toString().trim(),
                binding.shippingDetailsLayout.shippingAddress.text.toString().trim(),
                orderProducts,
                shippingFee,
                itemsPrice,
                totalPrice
            )

            Firebase.firestore.collection("Orders")
                .document(orderNumber)
                .set(order)
                .addOnSuccessListener {
                    loadingDialog.isDone("Order is placed successfully\nPlease use your order number as your payment reference") {
                        it.findViewById<LinearLayout>(R.id.order_layout).visibility = View.VISIBLE
                        it.findViewById<TextView>(R.id.order_number).text = orderNumber

                        orderProducts.forEach { pro ->
                            val productRef = Firebase.firestore.collection("Products")
                                .document(pro.code)
                            productRef.update("cartsUid", FieldValue.arrayRemove(Firebase.auth.currentUser!!.uid))
                            productRef.update("orderedCount", FieldValue.increment(pro.quantity.toLong()))
                            productRef.update("quantity", FieldValue.increment(-pro.quantity.toLong()))
                        }

                        it.setCancelable(true)
                        it.setOnDismissListener {
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }
                    }
                }
                .addOnFailureListener {
                    loadingDialog.isError(it.message)
                }
        }

    }
}