package com.example.wardrobe_share

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.wardrobe_share.viewModel.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var navController: NavController? = null
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Toolbar
        toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        // Initialize Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        navController = navHostFragment?.navController

        if (navController == null) {
            Log.e("MainActivity", "NavController is null. Check FragmentContainerView setup.")
            return
        }

        NavigationUI.setupActionBarWithNavController(this, navController!!)

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_bar)
        NavigationUI.setupWithNavController(bottomNavigationView, navController!!)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (navController?.currentDestination?.id != R.id.homeFragment) {
                        navController?.popBackStack(R.id.homeFragment, false)
                    } else {
                        navController?.popBackStack()
                    }
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController!!)
                }
            }
        }

        // Observe user authentication status
        observeUserAuthentication()
    }

    private fun observeUserAuthentication() {
        // Observe the authentication state
        authViewModel.user.observe(this, Observer { firebaseUser ->
            Log.d("TAG", "User: $firebaseUser")
            if (firebaseUser == null) {
                // Navigate to the Auth (login) screen if not authenticated
                startActivity(Intent(this, AuthActivity::class.java))
                finish() // Close MainActivity to prevent access without login
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
