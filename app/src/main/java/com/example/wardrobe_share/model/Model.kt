package com.example.wardrobe_share.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import com.example.wardrobe_share.base.EmptyCallback
import com.example.wardrobe_share.base.PostsCallback
import com.example.wardrobe_share.model.dao.AppLocalDb
import com.example.wardrobe_share.model.dao.AppLocalDbRepository
import com.google.firebase.auth.FirebaseUser
import android.os.Handler
import com.example.wardrobe_share.base.UsersCallback
import com.google.firebase.firestore.FirebaseFirestore
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
                val postsToFetch = posts.count { it.author.isNotEmpty() }
                if (postsToFetch == 0) {
                    roomExecutor.execute {
                        database.postDao().insertPosts(*posts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(posts)
                    }
                } else {
                    val updatedPosts = posts.toMutableList()
                    val counter = java.util.concurrent.atomic.AtomicInteger(postsToFetch)
                    for ((index, post) in updatedPosts.withIndex()) {
                        Log.d("Posts", "$index: Post: $post")

                        if (post.author.isNotEmpty() && post.author != null) {
                            getUser(post.author) { user ->
                                Log.d("getUserModel", "Model User Fetched: $user")


                                updatedPosts[index] = post.copy(
                                    authorName = user?.username ?: "",
                                    authorImage = user?.image ?: ""
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

    fun updatePost(post: Post, callback: EmptyCallback) {
        firebaseModel.updatePost(post) { success ->
            if (success) {
                roomExecutor.execute {
                    database.postDao().insertPosts(post)
                }
            }
            mainHandler.post { callback() }
        }
    }

    fun addPost(post: Post, profileImage: Bitmap?, callback: EmptyCallback) {
        firebaseModel.addPost(post) { firebaseSuccess ->
            if (!firebaseSuccess) {
                mainHandler.post { callback() }
                return@addPost
            }

            roomExecutor.execute {
                database.postDao().insertPosts(post)
            }

            if (profileImage != null) {
                uploadImageToCloudinary(
                    image = profileImage,
                    username = post.id,
                    onSuccess = { url ->
                        val updatedPost = post.copy(image = url)
                        firebaseModel.addPost(updatedPost) { updateSuccess ->
                            if (updateSuccess) {
                                roomExecutor.execute {
                                    database.postDao().insertPosts(updatedPost)
                                }
                            }
                            mainHandler.post { callback() }
                        }
                    },
                    onError = {
                        mainHandler.post { callback() }
                    }
                )
            } else {
                mainHandler.post { callback() }
            }
        }
    }

    fun deletePost(id: String, callback: EmptyCallback) {
        firebaseModel.deletePost(id) {
            roomExecutor.execute {
                database.postDao().deletePostById(id)
                mainHandler.post { callback() }
            }
        }
    }

    fun getAllUserPosts(id: String, callback: PostsCallback) {
        firebaseModel.getAllUserPosts(id) { posts ->
            if (posts.isNotEmpty()) {
                getUser(id) { user ->
                    val updatedPosts = posts.map { post ->
                        post.copy(
                            authorName = user?.username ?: "",
                            authorImage = user?.image ?: ""
                        )
                    }
                    roomExecutor.execute {
                        database.postDao().insertPosts(*updatedPosts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(updatedPosts)
                    }
                }
            } else {
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
        firebaseModel.getUser(id) { user ->
            Log.d("getUserBaseFunc", "User fetched: $user")
            if (user != null) {
                roomExecutor.execute {
                    database.userDao().insertUsers(user)
                }
                mainHandler.post { callback(user) }
            } else {
                roomExecutor.execute {
                    val localUser = database.userDao().getUserById(id)
                        ?: User(id, "Deleted User", "")
                    mainHandler.post { callback(localUser) }
                }
            }
        }
    }

    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signIn(email, password, callback)
    }


    fun signUp(email: String, password: String, username: String, bitmap: Bitmap?, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signUp(email, password) { firebaseUser, error ->
            if (firebaseUser != null) {
                if (bitmap != null) {
                    uploadImageToCloudinary(bitmap, firebaseUser.uid, onSuccess = { imageUrl ->
                        firebaseModel.saveUser(firebaseUser, username, imageUrl) { success, saveError ->
                            if (success) {
                                roomExecutor.execute {
                                    database.userDao().insertUsers(User(firebaseUser.uid, username, imageUrl))
                                }
                                mainHandler.post { callback(firebaseUser, null) }
                            } else {
                                mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
                            }
                        }
                    }, onError = { errMsg ->
                        Log.e("TAG", "Image upload to Cloudinary failed: $errMsg")
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
                    })
                } else {
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
            } else {
                mainHandler.post { callback(null, error ?: "Sign up failed") }
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

    fun updateUserProfile(userId: String, newUsername: String?, imageUrl: String?, callback: (Boolean) -> Unit) {
        val updates = mutableMapOf<String, Any>()
        if (!newUsername.isNullOrEmpty()) updates["username"] = newUsername
        if (!imageUrl.isNullOrEmpty()) updates["image"] = imageUrl

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
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