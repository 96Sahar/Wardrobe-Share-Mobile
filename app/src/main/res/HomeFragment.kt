package com.example.mixmaster

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
import com.example.mixmaster.adapter.OnPostClickListener
import com.example.mixmaster.adapter.PostListAdapter
import com.example.mixmaster.adapter.onUserClickListener
import com.example.mixmaster.databinding.FragmentHomeBinding
import com.example.mixmaster.model.Model
import com.example.mixmaster.model.Post
import com.example.mixmaster.model.User
import com.example.mixmaster.viewModel.AuthViewModel
import com.example.mixmaster.viewModel.PostViewModel

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
                Log.d("TAG", "On click listener on post: ${post?.name}")
                val action = HomeFragmentDirections
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
                        binding?.userNameTextView?.text = userObj.name ?: "Unknown"
                        if (userObj.image != "") {
                            if (binding?.profileImage != null) {
                                Glide.with(this).load(userObj.image).into(binding?.profileImage ?: return@runOnUiThread)
                            }
                        }
                        userLoaded = true
                        checkIfAllRequestsFinished()
                    }
                } else {
                    Log.d("TAG", "failed to fetch user")
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
                viewModel.set(posts = posts)
                adapter.set(posts)
                adapter.notifyDataSetChanged()
                postsLoaded = true
                checkIfAllRequestsFinished()
            }
        }
    }
}