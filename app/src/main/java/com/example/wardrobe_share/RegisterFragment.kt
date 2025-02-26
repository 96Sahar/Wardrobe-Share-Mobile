package com.example.wardrobe_share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wardrobe_share.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.signUpButton.setOnClickListener {
            registerUser()
        }

        binding.loginLink.setOnClickListener {
            navigateToLogin()
        }

    }

    private fun registerUser() {
        val username = binding.usernameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        // TODO: Implement actual user registration logic
        println("Registering user:")
        println("Username: $username")
        println("Email: $email")
        println("Password: $password")
    }

    private fun navigateToLogin() {
        (activity as? MainActivity)?.navigateToLogin()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}