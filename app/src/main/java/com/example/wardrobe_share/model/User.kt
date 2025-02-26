package com.example.wardrobe_share.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    val id:String,
    val username: String,
    val email:String,
    val password:String,
    val photo:String,
)
