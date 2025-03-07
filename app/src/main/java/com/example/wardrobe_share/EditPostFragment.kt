package com.example.wardrobe_share

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.wardrobe_share.databinding.FragmentEditPostBinding
import com.example.wardrobe_share.model.CloudinaryModel
import com.example.wardrobe_share.model.Model
import com.example.wardrobe_share.model.Post
import java.io.IOException

class EditPostFragment : Fragment() {
    private var binding: FragmentEditPostBinding? = null
    private var post: Post? = null
    private var selectedImageBitmap: Bitmap? = null
    private val cloudinaryModel = CloudinaryModel()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                selectedImageBitmap = bitmap
                binding?.imageView?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            selectedImageBitmap = it
            binding?.imageView?.setImageBitmap(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        post = arguments?.getParcelable("post")
        if (post == null) {
            Toast.makeText(context, "Post not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        binding?.descriptionInput?.setText(post?.description)
        binding?.locationInput?.setText(post?.location)
        binding?.phoneNumberInput?.setText(post?.phoneNumber)

        if (!post?.image.isNullOrEmpty()) {
            Glide.with(requireContext()).load(post?.image).into(binding?.imageView!!)
        }

        binding?.openImagesButton?.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding?.takePictureButton?.setOnClickListener { takePhotoLauncher.launch(null) }

        binding?.updateButton?.setOnClickListener { updatePost() }
    }

    private fun updatePost() {
        val description = binding?.descriptionInput?.text?.toString() ?: ""
        val location = binding?.locationInput?.text?.toString() ?: ""
        val phoneNumber = binding?.phoneNumberInput?.text?.toString() ?: ""

        if (description.isEmpty() || location.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_LONG).show()
            return
        }

        if (selectedImageBitmap != null) {
            cloudinaryModel.uploadBitmap(selectedImageBitmap!!, { imageUrl ->
                saveUpdatedPost(description, location, phoneNumber, imageUrl)
            }, { error ->
                Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
            })
        } else {
            saveUpdatedPost(description, location, phoneNumber, post?.image ?: "")
        }
    }

    private fun saveUpdatedPost(description: String, location: String, phoneNumber: String, imageUrl: String) {
        val updatedPost = post?.copy(
            description = description,
            location = location,
            phoneNumber = phoneNumber,
            image = imageUrl
        )

        if (updatedPost != null) {
            binding?.progressBar?.visibility = View.VISIBLE
            binding?.form?.visibility = View.GONE

            Model.shared.updatePost(updatedPost) {
                binding?.progressBar?.visibility = View.GONE
                binding?.form?.visibility = View.VISIBLE
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}