package com.example.wardrobe_share

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.wardrobe_share.databinding.FragmentRegisterBinding
import com.example.wardrobe_share.viewModel.AuthViewModel

class RegisterFragment : Fragment() {

    var binding: FragmentRegisterBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var didSetProfileImage = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            binding?.selectedImageView?.setImageURI(uri)
            didSetProfileImage = true
        } else {
            Log.e("SignUpFragment", "No image selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        val loginText = binding?.loginLink

        loginText?.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment2_to_loginFragment2)
        }
        binding?.signUpButton?.setOnClickListener(::onRegisterButtonClick)

        binding?.addPhotoButton?.setOnClickListener {
            pickImageLauncher.launch("image/*")
            binding?.selectedImageView?.visibility = View.VISIBLE
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            binding?.selectedImageView?.setImageBitmap(bitmap)
            didSetProfileImage = true
        }

        binding?.takePhotoButton?.setOnClickListener {
            binding?.selectedImageView?.visibility = View.VISIBLE
            cameraLauncher?.launch(null)
        }

        authViewModel.user.observe(viewLifecycleOwner, { user ->
            binding?.progressBar?.visibility = View.GONE
            binding?.registerLayout?.visibility = View.VISIBLE
            if (user != null) {
                goToHomeFragment()
            }
        })

        authViewModel.error.observe(viewLifecycleOwner, { errorMsg ->
            binding?.progressBar?.visibility = View.GONE
            binding?.registerLayout?.visibility = View.VISIBLE
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        return binding?.root
    }

    fun goToHomeFragment() {
        val intent = Intent(requireContext(), MainActivity::class.java);
        startActivity(intent);
        requireActivity().finish();
    }

    fun onRegisterButtonClick(view: View) {
        val username = binding?.usernameInput?.text.toString();
        val email = binding?.emailInput?.text.toString()
        val password1 = binding?.passwordInput?.text.toString()
        val password2 = binding?.confirmPasswordInput?.text.toString()

        if (username.isNotEmpty() && email.isNotEmpty() && password1.isNotEmpty() && password2.isNotEmpty()) {
            if (password1 == password2) {
                binding?.progressBar?.visibility = View.VISIBLE
                binding?.registerLayout?.visibility = View.GONE
                if(didSetProfileImage) {
                    binding?.selectedImageView?.isDrawingCacheEnabled = true
                    binding?.selectedImageView?.buildDrawingCache()
                    val bitmap = (binding?.selectedImageView?.drawable as BitmapDrawable).bitmap
                    authViewModel.signUp(email, password1, username, bitmap)
                } else {
                    Log.e("TAG", "No image selected")
                    authViewModel.signUp(email, password1, username, null)
                }
            } else {
                Toast.makeText(
                    requireContext(), "Passwords don't match.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(), "All fields are required.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}