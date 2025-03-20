package com.example.wardrobe_share

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.wardrobe_share.databinding.FragmentProfileBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wardrobe_share.adapter.PostListAdapter
import com.example.wardrobe_share.model.Model
import com.example.wardrobe_share.viewModel.AuthViewModel
import com.example.wardrobe_share.viewModel.PostViewModel

class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null
    private lateinit var postsAdapter: PostListAdapter
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var viewModel: PostViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PostViewModel::class.java]

        val currentUserId = authViewModel.user.value?.uid

        postsAdapter = PostListAdapter(emptyList(), currentUserId)
        binding?.postsRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }

        binding?.noPostsTextView?.visibility = View.VISIBLE

        if (currentUserId != null) {
            Model.shared.getUser(currentUserId) { userObj ->
                if (userObj != null) {
                    binding?.userName?.text = "Hey ${userObj.username}"

                    val imageUrl = if (userObj.image.isNullOrEmpty()) {
                        R.drawable.user
                    } else {
                        userObj.image
                    }

                    binding?.profileImage?.let { imageView ->
                        Glide.with(this).load(imageUrl).into(imageView)
                    }
                } else {
                    Log.e("ProfileFragment", "Failed to fetch user details")
                }
            }
            Model.shared.getAllUserPosts(currentUserId) { posts ->
                Log.d("ProfileFragment", "Fetched posts: $posts")

                if (posts.isNullOrEmpty()) {
                    binding?.noPostsTextView?.visibility = View.VISIBLE
                    postsAdapter.set(emptyList())
                } else {
                    binding?.noPostsTextView?.visibility = View.GONE
                    postsAdapter.set(posts)
                    postsAdapter.notifyDataSetChanged()
                }
            }
        } else {
            Log.e("ProfileFragment", "User is not signed in")
        }

        binding?.logoutButton?.setOnClickListener {
            authViewModel.signOut()
        }
        binding?.editProfileButton?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }



        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}







