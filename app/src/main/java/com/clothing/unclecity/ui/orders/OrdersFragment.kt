package com.clothing.unclecity.ui.orders

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.clothing.unclecity.activities.MainActivity
import com.clothing.unclecity.adapters.CartAdapter
import com.clothing.unclecity.adapters.OrdersAdapter
import com.clothing.unclecity.databinding.FragmentGalleryBinding
import com.clothing.unclecity.databinding.FragmentOrdersBinding
import com.clothing.unclecity.utils.OrderStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.StructuredQuery.Order

class OrdersFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        val isAdmin = requireContext().getSharedPreferences("User", Context.MODE_PRIVATE).getBoolean("isAdmin", false)

        val uid = Firebase.auth.currentUser!!.uid
        val query : Query = if (isAdmin) Firebase.firestore.collection("Orders")
        else Firebase.firestore.collection("Orders")
            .whereEqualTo("userUid", uid)

        val awaitingShippingQuery = query.whereEqualTo("orderStatus", OrderStatus.AWAITING_SHIPPING)
        val shippedQuery = query.whereEqualTo("orderStatus", OrderStatus.SHIPPED)
        val awaitingPaymentQuery = query.whereEqualTo("orderStatus", OrderStatus.AWAITING_PAYMENT)
        val completedQuery = query.whereEqualTo("orderStatus", OrderStatus.COMPLETED)

        query.addSnapshotListener { value, error ->
            if (error != null){
                return@addSnapshotListener
            }
            if (value?.isEmpty == true){
                binding.emptyLayout.visibility = View.VISIBLE
                binding.emptyEnim.playAnimation()
            } else {
                awaitingShippingQuery.addSnapshotListener { v, e ->
                    if (e == null){
                        if (v?.isEmpty != true){
                            binding.awaitingShippingText.visibility = View.VISIBLE
                        } else {
                            binding.awaitingShippingText.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                shippedQuery.addSnapshotListener { v, e ->
                    if (e == null){
                        if (v?.isEmpty != true){
                            binding.shippedText.visibility = View.VISIBLE
                        } else {
                            binding.shippedText.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                awaitingPaymentQuery.addSnapshotListener { v, e ->
                    if (e == null){
                        if (v?.isEmpty != true){
                            binding.awaitingPaymentText.visibility = View.VISIBLE
                        } else {
                            binding.awaitingPaymentText.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                completedQuery.addSnapshotListener { v, e ->
                    if (e == null){
                        if (v?.isEmpty != true){
                            binding.completedText.visibility = View.VISIBLE
                        } else {
                            binding.completedText.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        val awaitingShippingAdapter = OrdersAdapter( activity as MainActivity, awaitingShippingQuery)
        awaitingShippingAdapter.startListening()
        binding.awaitingShippingRecycler.layoutManager = LinearLayoutManager(context)
        binding.awaitingShippingRecycler.adapter = awaitingShippingAdapter

        val shippedAdapter = OrdersAdapter( activity as MainActivity,shippedQuery)
        shippedAdapter.startListening()
        binding.shippedRecycler.layoutManager = LinearLayoutManager(context)
        binding.shippedRecycler.adapter = shippedAdapter

        val awaitingPaymentAdapter = OrdersAdapter( activity as MainActivity,awaitingPaymentQuery)
        awaitingPaymentAdapter.startListening()
        binding.awaitingPaymentRecycler.layoutManager = LinearLayoutManager(context)
        binding.awaitingPaymentRecycler.adapter = awaitingPaymentAdapter

        val completedAdapter = OrdersAdapter( activity as MainActivity,completedQuery)
        completedAdapter.startListening()
        binding.completedRecycler.layoutManager = LinearLayoutManager(context)
        binding.completedRecycler.adapter = completedAdapter


        return binding.root
    }
}