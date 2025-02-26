package com.example.wardrobe_share

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.wardrobe_share.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            showLoginFragment()
        }
    }

    private fun showLoginFragment() {
        replaceFragment(LoginFragment())
    }

    private fun showRegisterFragment() {
        replaceFragment(RegisterFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }



    fun navigateToRegister() {
        showRegisterFragment()
    }

    fun navigateToLogin() {
        showLoginFragment()
    }
}