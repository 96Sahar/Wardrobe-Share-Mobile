package com.example.wardrobe_share.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val id: String = "",
    val username: String = "",
    val image: String = "",
) {

    companion object {

        private const val ID_KEY = "id"
        private const val USERNAME_KEY = "username"
        private const val IMAGE_KEY = "image"

        fun fromJSON(json: Map<String, Any>): User {
            val id = json[ID_KEY] as? String ?: ""
            val username = json[USERNAME_KEY] as? String ?: ""
            val image = json[IMAGE_KEY] as? String ?: ""

            return User(
                id = id,
                username = username,
                image = image,
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            USERNAME_KEY to username,
            IMAGE_KEY to image,
        )
}