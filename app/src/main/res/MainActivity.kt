package com.example.mixmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.mixmaster.viewModel.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var navController: NavController? = null
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavigationView: BottomNavigationView
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // auth check
        observeUserAuthentication()
        setContentView(R.layout.activity_main)

        // Handle system insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        bottomNavigationView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    // Use the global action to navigate back to HomeFragment
                    if (navController?.currentDestination?.id != R.id.homeFragment) {
                        navController?.popBackStack(R.id.homeFragment, false)
                    } else {
                        navController?.popBackStack();
                    }
                    true
                }
                else -> {
                    // For other items, let NavigationUI handle the navigation.
                    NavigationUI.onNavDestinationSelected(item, navController as NavController)
                }
            }
        }
    }

    private fun observeUserAuthentication() {
        // Observe the authentication state
        authViewModel.user.observe(this, Observer { firebaseUser ->
            Log.d("TAG", "User: $firebaseUser")
            if (firebaseUser == null) {
                // User is not authenticated. Navigate to the Auth (login) screen.
                startActivity(Intent(this, AuthActivity::class.java))
                finish() // Close MainActivity to prevent access without login
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController?.popBackStack();
                true
            }
            R.id.action_settings -> {
                if (navController?.currentDestination?.id == R.id.settingsFragment) {
                    return false
                }
                // Directly navigate to the settings fragment
                navController?.navigate(R.id.settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}