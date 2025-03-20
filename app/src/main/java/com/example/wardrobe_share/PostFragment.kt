package com.example.wardrobe_share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.wardrobe_share.model.Post
import de.hdodenhof.circleimageview.CircleImageView

class PostFragment : Fragment() {

    private lateinit var post: Post

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        post = arguments?.getParcelable("post") ?: return

        val sellerPhoto: CircleImageView = view.findViewById(R.id.user_image)
        val sellerName: TextView = view.findViewById(R.id.username_text)
        val productImage: ImageView = view.findViewById(R.id.product_image)
        val descriptionText: TextView = view.findViewById(R.id.description_text)
        val locationText: TextView = view.findViewById(R.id.location_text)
        val contactText: TextView = view.findViewById(R.id.contact_text)

        sellerName.text = post.authorName
        descriptionText.text = post.description
        locationText.text = post.location
        contactText.text = post.phoneNumber
        if (post.authorImage.isNotEmpty()) {
            Glide.with(this).load(post.authorImage).into(sellerPhoto)
        }

        if (post.image.isNotEmpty()) {
            Glide.with(this).load(post.image).into(productImage)
        }
    }
}
