package com.example.wardrobe_share.adapter

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
    listener: OnPostClickListener?,
    authorListener: onUserClickListener?,
    private val currentUserId: String?,
    private val adapter: PostListAdapter  // Add the adapter as a parameter
) : RecyclerView.ViewHolder(itemView) {

    private var post: Post? = null
    private val authorImage: CircleImageView = itemView.findViewById(R.id.sellerPhoto)
    private val authorName: TextView = itemView.findViewById(R.id.sellerName)
    private val postImage: ImageView = itemView.findViewById(R.id.productImage)
    private val postDescription: TextView = itemView.findViewById(R.id.itemDescription)
    private val location: TextView = itemView.findViewById(R.id.location)
    private val phoneNumber: TextView = itemView.findViewById(R.id.contact)
    private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
    private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

    init {
        postImage.setOnClickListener {
            listener?.onItemClick(post)
        }

        authorName.setOnClickListener {
            authorListener?.onItemClick(post?.author)
        }

        authorImage.setOnClickListener {
            authorListener?.onItemClick(post?.author)
        }

        deleteButton.setOnClickListener {
            post?.id?.let { postId ->
                Model.shared.deletePost(postId) {
                    Log.d("PostViewHolder", "Post with id $postId deleted.")
                    adapter.removePost(postId)  // Notify the adapter to remove the post
                }
            }
        }
    }

    fun bind(post: Post?, position: Int) {
        this.post = post
        authorName.text = post?.authorName
        postDescription.text = "Description: " + post?.description
        location.text = "Location: " + post?.location
        phoneNumber.text = "Phone Number: " + post?.phoneNumber

        // Load images using Glide
        Glide.with(itemView.context)
            .load(post?.authorImage)
            .placeholder(R.drawable.user)
            .into(authorImage)

        Glide.with(itemView.context)
            .load(post?.image)
            .placeholder(R.drawable.wardrobe_share_png_logo)
            .into(postImage)

        // Check if the current user is the author of the post
        if (post?.author == currentUserId) {
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
        } else {
            editButton.visibility = View.GONE
            deleteButton.visibility = View.GONE
        }
    }
}



