package com.clothing.unclecity.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.isDigitsOnly
import com.clothing.unclecity.R
import com.clothing.unclecity.databinding.AddEditProductActivityBinding
import com.clothing.unclecity.models.Category
import com.clothing.unclecity.models.Product
import com.clothing.unclecity.utils.Extensions.longToast
import com.clothing.unclecity.utils.Extensions.shortToast
import com.clothing.unclecity.utils.Extensions.tempDisable
import com.clothing.unclecity.utils.LoadingDialog
import com.clothing.unclecity.utils.OpenPicturesContract
import com.google.firebase.Timestamp
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: AddEditProductActivityBinding
    private lateinit var loadingDialog: LoadingDialog

    private var productImageUri : Uri? = null
    private val selectProductImageResult = registerForActivityResult(OpenPicturesContract()) { uri: Uri? ->
        uri?.let {
            productImageUri = it
            binding.productImage.setImageURI(it)
        }
    }

    private fun selectImage() {
        if (productImageUri == null) {
            selectProductImageResult.launch(arrayOf("image/*"))
        } else {
            val choice = arrayOf<CharSequence>("Change Image","Remove Image")
            AlertDialog.Builder(this).apply {
                setItems(choice) { _, item ->
                    when {
                        choice[item] == "Change Image" -> {
                            selectProductImageResult.launch(arrayOf("image/*"))
                        }
                        choice[item] == "Remove Image" -> {
                            binding.productImage.setImageURI(null)
                            productImageUri = null
                        }
                    }
                }
                show()
            }
        }
    }


    private val categoriesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = AddEditProductActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }


        Firebase.firestore.collection("Categories")
            .get()
            .addOnSuccessListener {
                it.toObjects(Category::class.java).forEach { cat ->
                    categoriesList.add(cat.name)
                }
                binding.category.setAdapter(ArrayAdapter(this, android.R.layout.simple_selectable_list_item, categoriesList))
            }


        binding.addProductImage.setOnClickListener {
            it.tempDisable()
            selectImage()
        }
        binding.productImage.setOnClickListener {
            it.tempDisable()
            selectImage()
        }

        binding.addProductButton.setOnClickListener {
            it.tempDisable(1000)
            validateAllInput {
                addProduct()
            }
        }

    }
    private fun addProduct() {
        loadingDialog.show("Saving Product...")

        val category = binding.category.text.toString().trim()
        if (!categoriesList.contains(category)){
            Firebase.firestore.collection("Categories")
                .document(category)
                .set(Category(category), SetOptions.merge())
        }

        val productName = binding.productName.editText!!.text.toString()

        val productCode = StringBuilder()
        productName.split(" ").forEach {
            if (it.isNotEmpty()){
                productCode.append(it[0])
            }
        }
        productCode.append(Timestamp.now().seconds.toString().takeLast(5))

        val sizes = binding.productSizes.editText!!.text.toString().split(',').toMutableList()
        sizes.forEach {
            if (it.isEmpty()){
                sizes.remove(it)
            }
        }
        val colors = binding.colors.editText!!.text.toString().split(',').toMutableList()
        colors.forEach {
            if (it.isEmpty()){
                colors.remove(it)
            }
        }

        val productStorageRef =  Firebase.storage.getReference("Products")
            .child("$productCode.png")
        val productUploadTask = productStorageRef.putFile(productImageUri!!)
        productUploadTask
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                productStorageRef.downloadUrl
            }
            .addOnSuccessListener { productImageUrl ->
                val discountedPrice = binding.discountedPrice.editText?.text?.trim()
                val product = Product(
                    productCode.toString(),productName,
                    binding.productBrand.editText!!.text.toString().trim(),
                    binding.productPrice.editText!!.text.toString().trim().toFloat(),
                    if (discountedPrice.isNullOrEmpty()) null
                    else discountedPrice.toString().toFloat(),
                    binding.productDesc.editText!!.text.toString().trim(),
                    productImageUrl.toString(),
                    category,
                    sizes.toList(),
                    colors.toList(),
                    quantity = binding.productsQuantity.editText!!.text.toString().trim().toLong()
                )
                Firebase.firestore.collection("Products")
                    .document(productCode.toString())
                    .set(product)
                    .addOnSuccessListener {
                        loadingDialog.isDone("Product added successfully") {
                            onBackPressed()
                        }
                    }
                    .addOnFailureListener {
                        loadingDialog.isError(it.message)
                    }
            }
            .addOnFailureListener {
                loadingDialog.isError(it.message)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun validateAllInput(success: () -> Unit) {
        if (binding.productName.editText!!.text.isNullOrEmpty()){
            shortToast("Product Name is Empty")
            binding.productName.editText!!.requestFocus()
            return
        }
        if (binding.productBrand.editText!!.text.isNullOrEmpty()){
            shortToast("Brand Name is Empty")
            binding.productBrand.editText!!.requestFocus()
            return
        }


        if (binding.productCategory.editText!!.text.isNullOrEmpty()){
            shortToast("Category is Empty")
            binding.productBrand.editText!!.requestFocus()
            return
        }

        if (binding.productPrice.editText!!.text.isNullOrEmpty()){
            shortToast("Price is Empty")
            binding.productPrice.editText!!.requestFocus()
            return
        }

        if (binding.productsQuantity.editText!!.text.isNullOrEmpty()){
            shortToast("Quantity is Empty")
            binding.productsQuantity.editText!!.requestFocus()
            return
        }

        if (binding.productDesc.editText!!.text.isNullOrEmpty()){
            shortToast("Description is Empty")
            binding.productDesc.editText!!.requestFocus()
            return
        }
        if (productImageUri == null) {
            shortToast("Select Product Image")
            return
        }
        if (binding.productSizes.editText!!.text.isNullOrEmpty()){
            shortToast("Sizes Empty")
            return
        }
        val sizes = binding.productSizes.editText!!.text.toString().split(',')
        val string = arrayListOf<String>()
        sizes.forEach {
            if (it.isDigitsOnly() && it.toInt() >= 100){
                string.add(it)
            }
        }
        if (string.isNotEmpty()){
            shortToast("Size ${string.joinToString(",")} too large." )
            return
        }

        if (binding.colors.editText!!.text.isNullOrEmpty()){
            shortToast("Colors Empty")
            return
        }

        success()
    }
}