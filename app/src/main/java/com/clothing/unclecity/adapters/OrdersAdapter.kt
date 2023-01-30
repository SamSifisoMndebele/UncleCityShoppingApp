package com.clothing.unclecity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clothing.unclecity.activities.MainActivity
import com.google.firebase.firestore.Query
import com.clothing.unclecity.R
import com.clothing.unclecity.activities.OrderDetailsActivity
import com.clothing.unclecity.models.Order
import com.clothing.unclecity.utils.Extensions.tempDisable
import com.clothing.unclecity.utils.Extensions.to2DecimalString
import com.clothing.unclecity.utils.OrderStatus
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OrdersAdapter(
    private val activity: MainActivity,
    query: Query
) : FireStoreAdapter<OrdersAdapter.ViewHolder>(query) {

    private val products = getSnapshots()
    private val isAdmin = activity.getSharedPreferences("User", Context.MODE_PRIVATE).getBoolean("isAdmin", false)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val userName: TextView = itemView.findViewById(R.id.user_name)
        private val orderTime: TextView = itemView.findViewById(R.id.order_time)
        private val orderNumber: TextView = itemView.findViewById(R.id.order_number)
        private val totalPrice: TextView = itemView.findViewById(R.id.total_price)
        private val productsNames: TextView = itemView.findViewById(R.id.products_names)
        private val orderStatus: TextView = itemView.findViewById(R.id.order_status)
        private val productQuantity: TextView = itemView.findViewById(R.id.product_quantity)

        @SuppressLint("SetTextI18n")
        fun bind(order: Order) {
            orderStatus.text = order.orderStatus
            userName.text = order.userName
            orderTime.text = order.timestamp.toDate().toString().take(16)
            orderNumber.text = order.orderNumber
            totalPrice.text = "R ${order.totalPrice.to2DecimalString()}"
            itemView.setOnClickListener {
                it.tempDisable()
                activity.startActivity(Intent(activity, OrderDetailsActivity::class.java).apply {
                    putExtra("Order", order)
                })
            }

            itemView.setOnLongClickListener {
                true
            }

            var items = 0
            val products = mutableListOf<String>()
            order.orderProducts.forEach {
                products.add(it.name)
                items += it.quantity
            }
            productsNames.text = products.joinToString(", ")
            productQuantity.text = "Qty: $items"

            if (isAdmin){
                orderStatus.setOnClickListener {

                    AlertDialog.Builder(activity).apply {
                        setTitle("Change Status to")
                        setItems(OrderStatus.array) { _, item ->
                            Firebase.firestore.collection("Orders")
                                .document(order.orderNumber)
                                .update("orderStatus",OrderStatus.array[item] )
                        }
                        show()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_order, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (products.size > position){
            holder.bind(products[position].toObject(Order::class.java)!!)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }
}