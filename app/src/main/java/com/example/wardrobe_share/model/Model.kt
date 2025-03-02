package com.example.wardrobe_share.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import com.example.wardrobe_share.base.EmptyCallback
import com.example.wardrobe_share.base.PostsCallback
import com.example.wardrobe_share.model.dao.AppLocalDb
import com.example.wardrobe_share.model.dao.AppLocalDbRepository
import com.google.firebase.auth.FirebaseUser
import android.os.Handler  // Use Android's Handler, not java.util.logging.Handler
import com.example.wardrobe_share.model.FirebaseModel
import com.example.wardrobe_share.base.UsersCallback
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executors

class Model private constructor() {

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()
    private val database: AppLocalDbRepository = AppLocalDb.database
    private var roomExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val shared = Model()
    }

    fun getAllPosts(callback: PostsCallback) {
        firebaseModel.getAllPosts { posts ->
            if (posts.isNotEmpty()) {
                val updatedPosts = posts.toMutableList()
                val counter = java.util.concurrent.atomic.AtomicInteger(posts.size)
                for ((index, post) in updatedPosts.withIndex()) {
                    getUser(post.author) { user ->
                        val username = user.username ?: "Unknown User"
                        val userImage = user?.image ?: ""
                        updatedPosts[index] = post.copy(
                            authorName = username,
                            authorImage = userImage
                        )
                        if (counter.decrementAndGet() == 0) {
                            roomExecutor.execute {
                                database.postDao().insertPosts(*updatedPosts.toTypedArray())
                            }
                            mainHandler.post {
                                callback(updatedPosts)
                            }
                        }
                    }
                }
            } else {
                roomExecutor.execute {
                    val localPosts = database.postDao().getAllPosts()
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
        }
    }

    fun getLastFourPosts(callback: PostsCallback) {
        firebaseModel.getLastFourPosts { posts ->
            Log.d("TAG", "Getting last four posts from Firebase: $posts")
            if (posts.isNotEmpty()) {
                // Count posts that have a non-empty author field.
                val postsToFetch = posts.count { it.author.isNotEmpty() }
                if (postsToFetch == 0) {
                    // No posts require author data – cache them locally and callback.
                    roomExecutor.execute {
                        database.postDao().insertPosts(*posts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(posts)
                    }
                } else {
                    // Create a mutable copy to update posts with user info.
                    val updatedPosts = posts.toMutableList()
                    // Use an atomic counter to track pending user fetches.
                    val counter = java.util.concurrent.atomic.AtomicInteger(postsToFetch)
                    for ((index, post) in updatedPosts.withIndex()) {
                        if (post.author.isNotEmpty()) {
                            getUser(post.author) { user ->
                                updatedPosts[index] = post.copy(
                                    authorName = user?.username ?: "",
                                    authorImage = user?.image ?: ""
                                )
                                // When all user fetches are complete, update local DB and trigger the callback.
                                if (counter.decrementAndGet() == 0) {
                                    roomExecutor.execute {
                                        database.postDao().insertPosts(*updatedPosts.toTypedArray())
                                    }
                                    mainHandler.post {
                                        callback(updatedPosts)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("TAG", "Getting last four posts from local database")
                roomExecutor.execute {
                    // It is assumed that your local DB provides a method to retrieve the last four posts.
                    // If not, you might fetch all posts and then filter/sort to get the last four.
                    val localPosts = database.postDao().getLastFourPosts()
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
        }
    }

    // Modify the addPost method to better handle image uploads
    fun addPost(post: Post, profileImage: Bitmap?, callback: EmptyCallback) {
        // If a profile image is provided, upload it first, then add the post with the image URL
        if (profileImage != null) {
            uploadImageToCloudinary(
                image = profileImage,
                username = post.id,
                onSuccess = { url ->
                    Log.d("Model", "Image uploaded successfully: $url")
                    val updatedPost = post.copy(image = url)
                    // Add the post with the image URL to Firebase
                    firebaseModel.addPost(updatedPost) { firebaseSuccess ->
                        if (firebaseSuccess) {
                            Log.d("Model", "Post with image added to Firebase")
                            roomExecutor.execute {
                                database.postDao().insertPosts(updatedPost)
                            }
                        } else {
                            Log.e("Model", "Failed to add post with image to Firebase")
                        }
                        mainHandler.post { callback() }
                    }
                },
                onError = { errorMsg ->
                    Log.e("Model", "Failed to upload image: $errorMsg")
                    // Add the post without an image
                    firebaseModel.addPost(post) { firebaseSuccess ->
                        if (firebaseSuccess) {
                            roomExecutor.execute {
                                database.postDao().insertPosts(post)
                            }
                        }
                        mainHandler.post { callback() }
                    }
                }
            )
        } else {
            // No image provided, just add the post
            firebaseModel.addPost(post) { firebaseSuccess ->
                if (firebaseSuccess) {
                    Log.d("Model", "Post added to Firebase (no image)")
                    roomExecutor.execute {
                        database.postDao().insertPosts(post)
                    }
                } else {
                    Log.e("Model", "Failed to add post to Firebase (no image)")
                }
                mainHandler.post { callback() }
            }
        }
    }

    fun getAllUserPosts(id: String, callback: PostsCallback) {
        firebaseModel.getAllUserPosts(id) { posts ->
            Log.d("TAG", "Getting user posts from Firebase: $posts")
            if (posts.isNotEmpty()) {
                // Fetch the user details once, since all posts share the same author.
                getUser(id) { user ->
                    val updatedPosts = posts.map { post ->
                        post.copy(
                            authorName = user?.username ?: "",
                            authorImage = user?.image ?: ""
                        )
                    }
                    // Cache updated posts locally.
                    roomExecutor.execute {
                        database.postDao().insertPosts(*updatedPosts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(updatedPosts)
                    }
                }
            } else {
                Log.d("TAG", "Getting user posts from local database")
                roomExecutor.execute {
                    val localPosts = database.postDao().getPostsByAuthor(id)
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
        }
    }

    fun getUser(id: String, callback: (User) -> Unit) {
        // Fetch the user from Firebase Authentication
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null && firebaseUser.uid == id) {
            // If the user is found in Firebase Authentication
            val username = firebaseUser.displayName ?: "Unknown User"
            val image = firebaseUser.photoUrl?.toString() ?: ""

            // Create a User object
            val user = User(
                id = firebaseUser.uid,
                username = username,
                image = image
            )

            Log.d("funcGetUser", "User fetched from Firebase Auth: id=${user.id}, username=${user.username}, image=${user.image}")
            callback(user)
        } else {
            // If the user is not found in Firebase Authentication, use a fallback
            Log.d("getUser", "User not found in Firebase Auth, using fallback: id=$id")
            val fallbackUser = User(id, "Unknown User", "")
            callback(fallbackUser)
        }
    }

    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signIn(email, password, callback)
    }


    // Modify the signUp method to better handle image uploads
    fun signUp(email: String, password: String, username: String, bitmap: Bitmap?, callback: (FirebaseUser?, String?) -> Unit) {
        Log.d("Model", "Sign up started")
        firebaseModel.signUp(email, password) { firebaseUser, error ->
            if (firebaseUser != null) {
                if (bitmap != null) {
                    Log.d("Model", "Uploading profile image to Cloudinary")
                    // Upload the image to Cloudinary
                    uploadImageToCloudinary(
                        image = bitmap,
                        username = firebaseUser.uid,
                        onSuccess = { imageUrl ->
                            Log.d("Model", "Image uploaded to Cloudinary: $imageUrl")
                            if (imageUrl.isNotEmpty()) {
                                // Save user data to Firestore with the Cloudinary image URL
                                firebaseModel.saveUser(firebaseUser, username, imageUrl) { success, saveError ->
                                    if (success) {
                                        // Save the user locally
                                        roomExecutor.execute {
                                            database.userDao().insertUsers(User(firebaseUser.uid, username, imageUrl))
                                        }
                                        mainHandler.post { callback(firebaseUser, null) }
                                    } else {
                                        mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
                                    }
                                }
                            } else {
                                Log.e("Model", "Empty image URL returned from Cloudinary")
                                saveUserWithoutImage(firebaseUser, username, callback)
                            }
                        },
                        onError = { errMsg ->
                            Log.e("Model", "Image upload to Cloudinary failed: $errMsg")
                            saveUserWithoutImage(firebaseUser, username, callback)
                        }
                    )
                } else {
                    saveUserWithoutImage(firebaseUser, username, callback)
                }
            } else {
                mainHandler.post { callback(null, error ?: "Sign up failed") }
            }
        }
    }

    // Helper method to avoid code duplication
    private fun saveUserWithoutImage(firebaseUser: FirebaseUser, username: String, callback: (FirebaseUser?, String?) -> Unit) {
        Log.d("Model", "Saving user without image")
        firebaseModel.saveUser(firebaseUser, username, "") { success, saveError ->
            if (success) {
                roomExecutor.execute {
                    database.userDao().insertUsers(User(firebaseUser.uid, username, ""))
                }
                mainHandler.post { callback(firebaseUser, null) }
            } else {
                mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
            }
        }
    }

    fun getAllUsers(callback: UsersCallback) {
        firebaseModel.getAllUsers { users ->
            if (users.isNotEmpty()) {
                roomExecutor.execute {
                    database.userDao().insertUsers(*users.toTypedArray())
                }
                mainHandler.post {
                    callback(users)
                }
            } else {
                roomExecutor.execute {
                    val localUsers = database.userDao().getAllUsers()
                    mainHandler.post {
                        callback(localUsers)
                    }
                }
            }
        }
    }


    fun signOut() {
        firebaseModel.signOut()
    }

    private fun uploadImageToFirebase(image: Bitmap, username: String, callback: (String?) -> Unit) {
        firebaseModel.uploadImage(image, username, callback)
    }

    private fun uploadImageToCloudinary(image: Bitmap, username: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        cloudinaryModel.uploadBitmap(
            bitmap = image,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}

