package com.example.wardrobe_share.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wardrobe_share.R
import com.example.wardrobe_share.model.Post

class PostListAdapter(
    private var posts: List<Post>?,
    private val currentUserId: String? // Add currentUserId parameter
) : RecyclerView.Adapter<PostViewHolder>() {

    var listener: OnPostClickListener? = null
    var authorListener: onUserClickListener? = null

    // Setter method to update the posts list
    fun set(posts: List<Post>?) {
        this.posts = posts
        notifyDataSetChanged()  // Notify the adapter to refresh the list
    }

    // Method to remove a post from the list
    fun removePost(postId: String) {
        // Remove the post with the matching postId
        posts = posts?.filter { it.id != postId }
        notifyDataSetChanged()  // Notify the adapter to refresh the list
    }

    override fun getItemCount(): Int = posts?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.post_holder,
            parent,
            false
        )
        // Pass the adapter to the ViewHolder to allow interaction
        return PostViewHolder(
            itemView,
            listener,
            authorListener,
            currentUserId,
            this  // Pass the adapter instance
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(
            post = posts?.get(position),
            position = position
        )
    }
}
