package com.clothing.unclecity.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.clothing.unclecity.adapters.CartAdapter
import com.clothing.unclecity.adapters.FavAdapter
import com.clothing.unclecity.adapters.ProductListAdapter
import com.clothing.unclecity.databinding.ActivityCartBinding
import com.clothing.unclecity.databinding.ActivityFavBinding
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.utils.LoadingDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FavActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavBinding
    private lateinit var loadingDialog : LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityFavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        loadingDialog = LoadingDialog(this)

        val query : Query = Firebase.firestore.collection("Products")
            .whereArrayContains("favoritesUid", Firebase.auth.currentUser!!.uid)

        query.addSnapshotListener { value, error ->
            if (error != null){
                return@addSnapshotListener
            }
            if (value?.isEmpty != false){
                binding.emptyLayout.visibility = View.VISIBLE
                binding.emptyEnim.playAnimation()
            } else {
                binding.emptyLayout.visibility = View.GONE
            }
        }


        val productAdapter = FavAdapter( this, query)
        productAdapter.startListening()


        binding.productsRecycler.layoutManager = LinearLayoutManager(this)
        binding.productsRecycler.adapter = productAdapter


    }
}