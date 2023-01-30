package com.clothing.unclecity.ui.shop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.clothing.unclecity.activities.MainActivity
import com.clothing.unclecity.R
import com.clothing.unclecity.activities.AddProductActivity
import com.clothing.unclecity.adapters.CategoriesListAdapter
import com.clothing.unclecity.adapters.ProductListAdapter
import com.clothing.unclecity.databinding.FragmentShopBinding
import com.clothing.unclecity.models.Category
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)


        val query : Query = Firebase.firestore.collection("Categories")
        val categoriesAdapter = CategoriesListAdapter( this.requireActivity() as MainActivity, query)
        categoriesAdapter.startListening()

        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.categoriesRecyclerView.setHasFixedSize(true)
        binding.categoriesRecyclerView.adapter = categoriesAdapter

        val isAdmin = requireContext().getSharedPreferences("User", Context.MODE_PRIVATE).getBoolean("isAdmin", false)
        if (isAdmin){
            binding.addProductFab.visibility = View.VISIBLE
            binding.addProductFab.setOnClickListener {
                startActivity(Intent(context, AddProductActivity::class.java))
            }
        }  else {
            binding.addProductFab.visibility = View.GONE
        }


        val searchFilterPref = requireContext().getSharedPreferences("search_filter", Context.MODE_PRIVATE)
        when(searchFilterPref.getString("id", "action_no_filter")){
            "action_no_filter" -> {
                binding.searchView.queryHint = "Search Product..."
            }
            "action_product_name" ->{
                binding.searchView.queryHint = "Search Product Name..."
            }
            "action_brand" ->{
                binding.searchView.queryHint = "Search Product Brand..."
            }
            "action_category" ->{
                binding.searchView.queryHint = "Search Product Category..."
            }
            "action_description" ->{
                binding.searchView.queryHint = "Search Product Description..."
            }
        }



        val shopViewModel = ViewModelProvider(activity as MainActivity)[ShopViewModel::class.java]
        binding.searchView.onActionViewExpanded()
        binding.searchView.clearFocus()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(string: String?): Boolean {
                binding.searchView.clearFocus()
                return false
            }
            override fun onQueryTextChange(string: String): Boolean {
                shopViewModel.setFilter(string)
                return false
            }
        })

        binding.searchFilter.setOnClickListener {
            val popupMenu = PopupMenu(context,binding.searchFilter)

            popupMenu.menuInflater.inflate(R.menu.filter_main, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_no_filter -> {
                        searchFilterPref.edit().putString("id", "action_no_filter").apply()
                        binding.searchView.queryHint = "Search Product..."
                    }
                    R.id.action_product_name -> {
                        searchFilterPref.edit().putString("id", "action_product_name").apply()
                        binding.searchView.queryHint = "Search Product Name..."
                    }
                    R.id.action_brand -> {
                        searchFilterPref.edit().putString("id", "action_brand").apply()
                        binding.searchView.queryHint = "Search Product Brand..."
                    }
                    R.id.action_category -> {
                        searchFilterPref.edit().putString("id", "action_category").apply()
                        binding.searchView.queryHint = "Search Product Category..."
                    }
                    R.id.action_description -> {
                        searchFilterPref.edit().putString("id", "action_description").apply()
                        binding.searchView.queryHint = "Search Product Description..."
                    }
                }
                true
            }
            popupMenu.show()
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}