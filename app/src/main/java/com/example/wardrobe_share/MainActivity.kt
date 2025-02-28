package com.example.wardrobe_share

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
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
import com.example.wardrobe_share.viewModel.AuthViewModel
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

        val button = findViewById<Button?>(R.id.logout)
        button?.setOnClickListener {
            authViewModel.signOut()
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

}