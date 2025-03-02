package com.example.wardrobe_share.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity
data class Post(
    @PrimaryKey val id: String = "",
    val image: String = "",
    val description: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    val author: String = "",      // The user ID of the author
    val authorName: String = "",  // The username of the author
    val authorImage: String = "",  // The profile image URL of the author
) {
    companion object {
        private const val ID_KEY = "id"
        private const val IMAGE_KEY = "image"
        private const val AUTHOR_KEY = "author"
        private const val AUTHORNAME_KEY = "authorName"
        private const val AUTHORIMAGE_KEY = "authorImage"
        private const val DESCRIPTION_KEY = "description"
        private const val PHONENUMBER_KEY = "phoneNumber"
        private const val LOCATION_KEY = "location"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? String ?: ""
            val image = json[IMAGE_KEY] as? String ?: ""
            val author = json[AUTHOR_KEY] as? String ?: ""
            val authorName = json[AUTHORNAME_KEY] as? String ?: ""
            val authorImage = json[AUTHORIMAGE_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val phoneNumber = json[PHONENUMBER_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? String ?: ""

            return Post(
                id = id,
                image = image,
                author = author,
                authorName = authorName,
                authorImage = authorImage,
                description = description,
                phoneNumber = phoneNumber,
                location = location
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            IMAGE_KEY to image,
            AUTHOR_KEY to author,
            AUTHORNAME_KEY to authorName,  // Ensure this is included
            AUTHORIMAGE_KEY to authorImage,
            DESCRIPTION_KEY to description,
            PHONENUMBER_KEY to phoneNumber,
            LOCATION_KEY to location
        )
}

