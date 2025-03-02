package com.example.wardrobe_share.adapter

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wardrobe_share.R
import com.example.wardrobe_share.model.Post
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale


interface OnPostClickListener {
    fun onItemClick(post: Post?)
}

interface onUserClickListener {
    fun onItemClick(id: String?)
}


class PostViewHolder(
    itemView: View,
    listener: OnPostClickListener?,
    authorListener: onUserClickListener?
): RecyclerView.ViewHolder(itemView) {
    private var post: Post? = null
    private val authorImage: CircleImageView = itemView.findViewById(R.id.sellerPhoto)
    private val authorName: TextView = itemView.findViewById(R.id.authorName)
    private val postImage: ImageView = itemView.findViewById(R.id.productImage)
    private val postDescription: TextView = itemView.findViewById(R.id.itemDescription)
    private val location: TextView = itemView.findViewById(R.id.location)
    private val phoneNumber: TextView = itemView.findViewById(R.id.contact)


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
    }

    // Modify the bind method to better handle image loading
    fun bind(post: Post?, position: Int) {

        this.post = post
        Log.d("ThePosts", "Posts details: $post")

        // Set author name with a fallback
        val displayName = if (post?.authorName?.isNotEmpty() == true) {
            post.authorName
        } else {
            "Unknown User"
        }
        Log.d("PostViewHolder", "Binding post: ${post?.id}, authorName: $displayName")
        authorName.text = displayName

        postDescription.text = "Description: " + post?.description
        location.text = "Location: " + post?.location
        phoneNumber.text = "Phone Number: " + post?.phoneNumber

        // Load author image using Glide
        if (!post?.authorImage.isNullOrEmpty()) {
            Glide.with(itemView.context)
                .load(post?.authorImage)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(authorImage)
        } else {
            // Set default image if author image is empty
            authorImage.setImageResource(R.drawable.user)
        }

        // Load post image using Glide
        if (!post?.image.isNullOrEmpty()) {
            Glide.with(itemView.context)
                .load(post?.image)
                .placeholder(R.drawable.wardrobe_share_png_logo)
                .error(R.drawable.wardrobe_share_png_logo)
                .into(postImage)
        } else {
            // Set default image if post image is empty
            postImage.setImageResource(R.drawable.wardrobe_share_png_logo)
        }
    }
}

