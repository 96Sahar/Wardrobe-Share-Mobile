package com.example.wardrobe_share.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wardrobe_share.R
import com.example.wardrobe_share.model.Post

class PostListAdapter(private var posts: List<Post>?): RecyclerView.Adapter<PostViewHolder>() {

    var listener: OnPostClickListener? = null
    var authorListener: onUserClickListener? = null

    fun set(posts: List<Post>?) {
        this.posts = posts
    }

    override fun getItemCount(): Int = posts?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.post_holder,
            parent,
            false
        )
        return PostViewHolder(itemView, listener, authorListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(
            post = posts?.get(position),
            position = position
        )
    }
}