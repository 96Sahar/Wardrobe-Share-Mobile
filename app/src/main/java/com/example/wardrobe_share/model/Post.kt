package com.example.wardrobe_share.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey val id: String,
    val description: String,
    val image: String,
    val userId: String,
    val city: String,
    val phone: String,

)
