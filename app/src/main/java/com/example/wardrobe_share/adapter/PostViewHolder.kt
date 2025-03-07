package com.example.wardrobe_share.adapter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wardrobe_share.EditPostFragment
import com.example.wardrobe_share.R
import com.example.wardrobe_share.model.Model
import com.example.wardrobe_share.model.Post
import de.hdodenhof.circleimageview.CircleImageView



interface OnPostClickListener {
    fun onItemClick(post: Post?)
}

interface onUserClickListener {
    fun onItemClick(id: String?)
}


class PostViewHolder(
    itemView: View,
    private val listener: OnPostClickListener?,
    private val authorListener: onUserClickListener?,
    private val currentUserId: String?,
    private val adapter: PostListAdapter
) : RecyclerView.ViewHolder(itemView) {

    private val sellerPhoto: ImageView = itemView.findViewById(R.id.sellerPhoto)
    private val sellerName: TextView = itemView.findViewById(R.id.sellerName)
    private val productImage: ImageView = itemView.findViewById(R.id.productImage)
    private val itemDescription: TextView = itemView.findViewById(R.id.itemDescription)
    private val location: TextView = itemView.findViewById(R.id.location)
    private val contact: TextView = itemView.findViewById(R.id.contact)
    private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
    private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

    fun bind(post: Post?, position: Int) {
        if (post == null) return

        // Bind post data to views
        sellerName.text = post.authorName
        itemDescription.text = "Description: " + post.description
        location.text = "Location: " + post.location
        contact.text = "Phone number: " + post.phoneNumber

        // Load seller photo using Glide
        if (post.authorImage.isNotEmpty()) {
            Glide.with(itemView.context)
                .load(post.authorImage)
                .into(sellerPhoto)
        }

        // Load product image using Glide
        if (post.image.isNotEmpty()) {
            Glide.with(itemView.context)
                .load(post.image)
                .into(productImage)
        }

        // Show edit and delete buttons only if the current user is the author of the post
        if (post.author == currentUserId) {
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
        } else {
            editButton.visibility = View.GONE
            deleteButton.visibility = View.GONE
        }

        // Set click listener for the edit button
        editButton.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("post", post) // Pass the post to EditPostFragment
            }
            itemView.findNavController().navigate(R.id.editPostFragment, bundle)
        }

        // Set click listener for the delete button
        deleteButton.setOnClickListener {
            post?.id?.let { postId ->
                Model.shared.deletePost(postId) {
                    Log.d("PostViewHolder", "Post with id $postId deleted.")
                    adapter.removePost(postId)  // Notify the adapter to remove the post
                }
            }
        }

        // Set click listener for the seller photo to navigate to the author's profile
        sellerPhoto.setOnClickListener {
            authorListener?.onItemClick(post.author)
        }
    }
}



