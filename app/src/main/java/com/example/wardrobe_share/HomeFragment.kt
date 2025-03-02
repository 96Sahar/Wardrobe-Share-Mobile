package com.example.wardrobe_share

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wardrobe_share.adapter.OnPostClickListener
import com.example.wardrobe_share.adapter.PostListAdapter
import com.example.wardrobe_share.adapter.onUserClickListener
import com.example.wardrobe_share.databinding.FragmentHomeBinding
import com.example.wardrobe_share.model.Model
import com.example.wardrobe_share.model.Post
import com.example.wardrobe_share.model.User
import com.example.wardrobe_share.viewModel.AuthViewModel
import com.example.wardrobe_share.viewModel.PostViewModel

class HomeFragment : Fragment() {

    // Use a backing property for binding
    private var binding: FragmentHomeBinding? = null

    private lateinit var viewModel: PostViewModel
    private lateinit var adapter: PostListAdapter
    // Use AuthViewModel only to obtain the user id.
    private val authViewModel: AuthViewModel by activityViewModels()

    // Flags to track when each request finishes
    private var postsLoaded = false
    private var userLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PostViewModel::class.java]

        // Setup RecyclerView for posts.
        binding?.homeRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        // Initialize adapter with an empty list
        adapter = PostListAdapter(emptyList())
        adapter.listener = object : OnPostClickListener {
            override fun onItemClick(post: Post?) {
                Log.d("TAG", "On click listener on post: ${post?.id}")
            }
        }
        adapter.authorListener = object : onUserClickListener {
            override fun onItemClick(id: String?) {
                Log.d("TAG", "On click listener on author: ${id}")
                findNavController().navigate(R.id.profileFragment, Bundle().apply {
                    putString("userId", id)
                })
            }
        }
        binding?.homeRecyclerView?.adapter = adapter

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        // Reset flags and show the progress bar
        postsLoaded = false
        userLoaded = false
        binding?.progressBar?.visibility = View.VISIBLE

        Model.shared.getAllUsers {
            Log.d("TAG", "Users: $it")
        }

        getAllPosts()
        getUserDetails()
    }

    private fun checkIfAllRequestsFinished() {
        if (postsLoaded && userLoaded) {
            binding?.progressBar?.visibility = View.GONE
        }
    }

    private fun getUserDetails() {
        val user = authViewModel.user.value
        if (user != null) {
            Log.d("TAG", "User is signed in: ${user.uid}")
            Model.shared.getUser(user.uid) { userObj: User? ->
                if (userObj != null) {
                    activity?.runOnUiThread {
                        Log.d("TAG", "Fetched user: $userObj")
                        binding?.userNameTextView?.text = userObj.username ?: "Unknown"

                        // Set the profile image
                        if (!userObj.image.isNullOrEmpty()) {
                            // Load the user's profile image using Glide
                            Glide.with(this)
                                .load(userObj.image)
                                .placeholder(R.drawable.wardrobe_share_png_logo) // Default image while loading
                                .error(R.drawable.wardrobe_share_png_logo) // Default image if loading fails
                                .into(binding?.profileImage ?: return@runOnUiThread)
                        } else {
                            // If the user doesn't have an image, set the default image
                            binding?.profileImage?.setImageResource(R.drawable.user)
                        }

                        userLoaded = true
                        checkIfAllRequestsFinished()
                    }
                } else {
                    Log.d("TAG", "Failed to fetch user")
                    userLoaded = true
                    checkIfAllRequestsFinished()
                }
            }
        } else {
            Log.d("TAG", "User is not signed in")
            userLoaded = true
            checkIfAllRequestsFinished()
        }
    }

    private fun getAllPosts() {
        Model.shared.getAllPosts { posts ->
            activity?.runOnUiThread {
                Log.d("HomeFragment", "Fetched posts: ${posts.size} items")
                viewModel.set(posts = posts)
                adapter.set(posts)
                adapter.notifyDataSetChanged()
                postsLoaded = true
                checkIfAllRequestsFinished()
            }
        }
    }
}