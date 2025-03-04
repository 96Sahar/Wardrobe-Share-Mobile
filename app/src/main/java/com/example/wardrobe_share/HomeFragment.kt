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
            }
        }
        adapter.authorListener = object : onUserClickListener {
            override fun onItemClick(id: String?) {
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
            Model.shared.getUser(user.uid) { userObj: User? ->
                if (userObj != null) {
                    activity?.runOnUiThread {
                        binding?.userNameTextView?.text = userObj.username ?: "Unknown"
                        if (userObj.image != "") {
                            if (binding?.profileImage != null) {
                                Glide.with(this).load(userObj.image).into(binding?.profileImage ?: return@runOnUiThread)
                            }
                        }
                        userLoaded = true
                        checkIfAllRequestsFinished()
                    }
                } else {
                    Log.e("TAG", "failed to fetch user")
                    userLoaded = true
                    checkIfAllRequestsFinished()
                }
            }
        } else {
            Log.e("TAG", "User is not signed in")
            userLoaded = true
            checkIfAllRequestsFinished()
        }
    }

    private fun getAllPosts() {
        Model.shared.getAllPosts { posts ->
            Log.d("getAllPostsHomeFragment", "HomeFragment posts fetched: $posts")
            activity?.runOnUiThread {
                viewModel.set(posts = posts)
                adapter.set(posts)
                adapter.notifyDataSetChanged()
                postsLoaded = true
                checkIfAllRequestsFinished()
            }
        }
    }
}