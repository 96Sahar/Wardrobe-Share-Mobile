package com.example.wardrobe_share

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.wardrobe_share.databinding.FragmentEditProfileBinding
import com.example.wardrobe_share.model.CloudinaryModel
import com.example.wardrobe_share.model.Model
import com.example.wardrobe_share.viewModel.AuthViewModel
import java.io.IOException

class EditProfileFragment : Fragment() {
    private var binding: FragmentEditProfileBinding? = null
    private val authViewModel: AuthViewModel by activityViewModels()
    private var selectedImageBitmap: Bitmap? = null
    private val cloudinaryModel = CloudinaryModel()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                selectedImageBitmap = bitmap
                binding?.imageProfile?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            selectedImageBitmap = it
            binding?.imageProfile?.setImageBitmap(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        val currentUserId = authViewModel.user.value?.uid
        if (currentUserId != null) {
            // Load user details
            Model.shared.getUser(currentUserId) { user ->
                user?.let {
                    binding?.editUsername?.setText(it.username)
                    if (!it.image.isNullOrEmpty()) {
                        Glide.with(this).load(it.image).into(binding?.imageProfile!!)
                    } else {
                        binding?.imageProfile?.setImageResource(R.drawable.ic_person)
                    }
                }
            }
        }

        binding?.btnPickPhoto?.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding?.btnTakePhoto?.setOnClickListener { takePhotoLauncher.launch(null) }

        binding?.btnEditProfile?.setOnClickListener { saveProfileChanges() }

        return binding!!.root
    }

    private fun saveProfileChanges() {
        val newUsername = binding?.editUsername?.text.toString().trim()

        val currentUserId = authViewModel.user.value?.uid
        if (currentUserId != null) {
            if (selectedImageBitmap != null) {
                // Upload image to Cloudinary
                cloudinaryModel.uploadBitmap(selectedImageBitmap!!, { imageUrl ->
                    updateUserProfile(currentUserId, newUsername, imageUrl)
                }, { error ->
                    Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
                })
            } else {
                // No new image selected, just update username
                updateUserProfile(currentUserId, newUsername, null)
            }
        }
    }

    private fun updateUserProfile(userId: String, username: String, imageUrl: String?) {
        Model.shared.updateUserProfile(userId, username, imageUrl ?: "") { success ->
            if (success) {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
