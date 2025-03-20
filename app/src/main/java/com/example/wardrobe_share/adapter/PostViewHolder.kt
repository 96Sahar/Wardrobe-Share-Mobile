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
import com.example.wardrobe_share.R
import com.example.wardrobe_share.model.Model
import com.example.wardrobe_share.model.Post



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

        sellerName.text = post.authorName
        itemDescription.text = "Description: " + post.description
        location.text = "Location: " + post.location
        contact.text = "Phone number: " + post.phoneNumber

        if (post.authorImage.isNotEmpty()) {
            Glide.with(itemView.context)
                .load(post.authorImage)
                .into(sellerPhoto)
        }

        if (post.image.isNotEmpty()) {
            Glide.with(itemView.context)
                .load(post.image)
                .into(productImage)
        }

        if (post.author == currentUserId) {
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
        } else {
            editButton.visibility = View.GONE
            deleteButton.visibility = View.GONE
        }

        editButton.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("post", post)
            }
            itemView.findNavController().navigate(R.id.editPostFragment, bundle)
        }

        deleteButton.setOnClickListener {
            post.id?.let { postId ->
                Model.shared.deletePost(postId) {
                    Log.d("PostViewHolder", "Post with id $postId deleted.")
                    adapter.removePost(postId)
                }
            }
        }

        sellerPhoto.setOnClickListener {
            authorListener?.onItemClick(post.author)
        }

        itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("post", post)
            }
            itemView.findNavController().navigate(R.id.postFragment, bundle)
        }
    }
}




