package com.example.wardrobe_share.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey val id: String = "",
    val image: String = "",
    val description: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    val author: String = "",
    val authorName: String = "",
    val authorImage: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    companion object {
        private const val ID_KEY = "id"
        private const val IMAGE_KEY = "image"
        private const val AUTHOR_KEY = "author"
        private const val AUTHORNAME_KEY = "authorName"
        private const val DESCRIPTION_KEY = "description"
        private const val PHONENUMBER_KEY = "phoneNumber"
        private const val LOCATION_KEY = "location"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? String ?: ""
            val image = json[IMAGE_KEY] as? String ?: ""
            val author = json[AUTHOR_KEY] as? String ?: ""
            val authorName = json[AUTHORNAME_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val phoneNumber = json[PHONENUMBER_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? String ?: ""
            return Post(id = id, image = image, author = author,
                authorName = authorName,
                description = description, phoneNumber = phoneNumber,
                location = location)
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<Post> {
            override fun createFromParcel(parcel: Parcel): Post {
                return Post(parcel)
            }

            override fun newArray(size: Int): Array<Post?> {
                return arrayOfNulls(size)
            }
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            IMAGE_KEY to image,
            AUTHOR_KEY to author,
            AUTHORNAME_KEY to authorName,
            DESCRIPTION_KEY to description,
            PHONENUMBER_KEY to phoneNumber,
            LOCATION_KEY to location
        )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(phoneNumber)
        parcel.writeString(location)
        parcel.writeString(author)
        parcel.writeString(authorName)
        parcel.writeString(authorImage)
    }

    override fun describeContents(): Int {
        return 0
    }
}

