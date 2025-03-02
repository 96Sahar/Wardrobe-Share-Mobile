package com.example.mixmaster

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.mixmaster.databinding.FragmentSettingsBinding
import com.example.mixmaster.viewModel.AuthViewModel


class SettingsFragment : Fragment() {

    private var binding: FragmentSettingsBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding?.logoutButton?.setOnClickListener{
            authViewModel.signOut()
        }

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}