package com.example.wardrobe_share.adapter

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
    private val authorName: TextView = itemView.findViewById(R.id.sellerName)
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

    fun bind(post: Post?, position: Int) {
        this.post = post
        authorName.text = post?.authorName
        postDescription.text = post?.description
        location.text = post?.location
        phoneNumber.text = post?.phoneNumber


        // Load images using Glide
        Glide.with(itemView.context)
            .load(post?.authorImage)
            .placeholder(R.drawable.user)
            .into(authorImage)

        Glide.with(itemView.context)
            .load(post?.image)
            .placeholder(R.drawable.wardrobe_share_png_logo)
            .into(postImage)
    }
}
