package com.example.wardrobe_share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wardrobe_share.databinding.FragmentEditPostBinding
import com.example.wardrobe_share.model.Model
import com.example.wardrobe_share.model.Post

class EditPostFragment : Fragment() {

    private var binding: FragmentEditPostBinding? = null
    private var post: Post? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the post from arguments
        post = arguments?.getParcelable("post")
        if (post == null) {
            Toast.makeText(context, "Post not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        // Pre-fill the fields with post data
        binding?.descriptionInput?.setText(post?.description)
        binding?.locationInput?.setText(post?.location)
        binding?.phoneNumberInput?.setText(post?.phoneNumber)

        // Set click listener for the update button
        binding?.updateButton?.setOnClickListener {
            updatePost()
        }
    }

    private fun updatePost() {
        val description = binding?.descriptionInput?.text?.toString() ?: ""
        val location = binding?.locationInput?.text?.toString() ?: ""
        val phoneNumber = binding?.phoneNumberInput?.text?.toString() ?: ""

        if (description.isEmpty() || location.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_LONG).show()
            return
        }

        // Create an updated post object
        val updatedPost = post?.copy(
            description = description,
            location = location,
            phoneNumber = phoneNumber
        )

        if (updatedPost != null) {
            binding?.progressBar?.visibility = View.VISIBLE
            binding?.form?.visibility = View.GONE

            // Update the post in Firebase and local DB
            Model.shared.updatePost(updatedPost) {
                binding?.progressBar?.visibility = View.GONE
                binding?.form?.visibility = View.VISIBLE
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Go back to the previous fragment
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}