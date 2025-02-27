package com.example.wardrobe_share.viewModel

import androidx.lifecycle.ViewModel
import com.example.wardrobe_share.model.Post

class PostViewModel : ViewModel() {

    private var _posts: List<Post>? = null
    var posts: List<Post>?
        get() = _posts
        private set(value) {
            _posts = value
        }

    fun set(posts: List<Post>?) {
        this.posts = posts
    }
}