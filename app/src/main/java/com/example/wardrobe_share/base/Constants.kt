package com.example.wardrobe_share.base

import com.example.wardrobe_share.model.Post
import com.example.wardrobe_share.model.User

typealias PostsCallback = (List<Post>) -> Unit
typealias EmptyCallback = () -> Unit
typealias SuccessCallback = (Boolean) -> Unit
typealias UsersCallback = (List<User>) -> Unit

object Constants {

    object COLLECTIONS {
        const val POSTS = "posts"
        const val USERS = "users"
    }
}