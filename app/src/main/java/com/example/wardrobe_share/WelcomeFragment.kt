package com.example.wardrobe_share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wardrobe_share.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var binding: FragmentWelcomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        val loginButton: Button? = binding?.loginButton

        loginButton?.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment2_to_loginFragment2)
        }

        val registerButton: Button? = binding?.registerButton

        registerButton?.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment2_to_registerFragment2)
        }

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}