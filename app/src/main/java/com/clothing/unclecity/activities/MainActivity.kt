package com.clothing.unclecity.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.clothing.unclecity.R
import com.clothing.unclecity.databinding.ActivityMainBinding
import com.clothing.unclecity.models.StoreInfo
import com.clothing.unclecity.ui.profile.ProfileViewModel
import com.clothing.unclecity.utils.LoadingDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var isAdmin : Boolean = false
    lateinit var loadingDialog : LoadingDialog
    val profileViewModel : ProfileViewModel
        get() = ViewModelProvider(this)[ProfileViewModel::class.java]

    private var storeInfo : StoreInfo = StoreInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)
        isAdmin = getSharedPreferences("User", Context.MODE_PRIVATE).getBoolean("isAdmin", false)
        setSupportActionBar(binding.appBarMain.toolbar)

        Firebase.firestore.collection("Store").document("StoreInfo")
            .get().addOnSuccessListener {
                storeInfo = it?.toObject<StoreInfo>()?:StoreInfo()
            }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_store_info, R.id.nav_orders
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        profileViewModel.email.observe(this) {
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.user_email).text = it
        }
        profileViewModel.name2.observe(this) {
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.user_name).text = it
        }
        profileViewModel.imageUrl.observe(this) {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(binding.navView.getHeaderView(0).findViewById(R.id.user_image))
        }

        binding.navView.menu.findItem(R.id.nav_store_info).isVisible = isAdmin

        val whatsappMenu = binding.navView.menu.findItem(R.id.action_whatsapp)
        val queryMenu = binding.navView.menu.findItem(R.id.action_query)
        whatsappMenu.isVisible = !isAdmin
        queryMenu.isVisible = !isAdmin
        if (!isAdmin){
            whatsappMenu.setOnMenuItemClickListener {
                val url = "https://api.whatsapp.com/send?phone=" + storeInfo.businessNumber
                val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                whatsappIntent.setPackage("com.whatsapp")
                whatsappIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                try {
                    startActivity(whatsappIntent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(this, "App is not installed", Toast.LENGTH_SHORT).show()
                }
                true
            }
            queryMenu.setOnMenuItemClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.data = Uri.parse(storeInfo.queryFormLink)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
                true
            }
        }



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val query = Firebase.firestore.collection("Products")

        query.whereArrayContains("favoritesUid", Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                menu.findItem(R.id.action_fav).isVisible = value?.isEmpty == false
            }
        query.whereArrayContains("cartsUid", Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                menu.findItem(R.id.action_cart).isVisible = value?.isEmpty == false
            }


        menu.setGroupVisible(0, !isAdmin)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_fav -> {
                startActivity(Intent(this, FavActivity::class.java))
                true
            }
            R.id.action_cart -> {
                startActivity(Intent(this, CartActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}