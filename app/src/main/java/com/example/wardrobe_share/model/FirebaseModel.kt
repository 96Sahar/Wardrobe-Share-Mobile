package com.example.wardrobe_share.model

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.storage.storage
import com.example.wardrobe_share.base.Constants
import com.example.wardrobe_share.base.EmptyCallback
import com.example.wardrobe_share.base.PostsCallback
import com.example.wardrobe_share.base.SuccessCallback
import com.example.wardrobe_share.base.UsersCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.ByteArrayOutputStream

class FirebaseModel {

    private val database = Firebase.firestore
    private val storage = Firebase.storage
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }

        database.firestoreSettings = setting
    }

    fun getAllPosts(callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()

                        for (json in it.result) {
                            posts.add(Post.fromJSON(json.data))
                            Log.d("getAllPostsFromFireBaseModel", "posts: ${json.data}")
                            Log.d("getAllPosts", "FirebaseModel posts fetched: ${json.data}")

                        }
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
    }


    fun getAllUserPosts(id: String, callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS)
            .whereEqualTo("author", id)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            posts.add(Post.fromJSON(json.data))
                        }
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun updatePost(post: Post, callback: SuccessCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(post.id)
            .set(post.json)
            .addOnCompleteListener {
                callback(it.isSuccessful)
            }
    }

    fun addPost(post: Post, callback: SuccessCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(post.id)
            .set(post.json)
            .addOnCompleteListener {
                callback(it.isSuccessful)
            }
    }

    fun deletePost(id: String, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(id).delete()
            .addOnCompleteListener {
                callback()
            }
    }

    fun signUp(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    callback(user, null)
                } else {
                    Log.d("Here", "Here")
                    callback(null, task.exception?.message)
                }
            }
    }


    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(auth.currentUser, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun saveUser(user: FirebaseUser, username: String, image: String?, callback: (Boolean, String?) -> Unit) {
        val userData = hashMapOf(
            "id" to user.uid,
            "image" to image,
            "username" to username,
        )

        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.uid)
            .set(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun getUser(id: String, callback: (User?) -> Unit) {
        database.collection(Constants.COLLECTIONS.USERS).document(id).get()
            .addOnCompleteListener { task ->
                Log.d("getUserFireBaseFunc", "Task completed: ${task.result.data?.values}")

                if (task.isSuccessful && task.result != null && task.result.exists()) {
                    val document = task.result
                    val data = document.data

                    Log.d("getUserFireBaseFuncData", "Document data: $data")

                    if (data != null) {
                        val userId = data["id"] as? String ?: id
                        val userName = data["username"] as? String ?: ""
                        val userImage = data["image"] as? String ?: ""

                        Log.d("getUserFireBaseFunc", "User ID: $userId, User Name: $userName, User Image: $userImage")

                        val user = User(
                            id = userId,
                            username = userName,
                            image = userImage
                        )

                        Log.d("getUserFireBaseFunc", "User created manually: $user")
                        callback(user)
                    } else {
                        Log.d("getUserFireBaseFunc", "Document exists but data is null")
                        callback(null)
                    }
                } else {
                    Log.d("getUserFireBaseFunc", "Task failed or document doesn't exist")
                    callback(null)
                }
            }
    }



    fun getAllUsers(callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (json in it.result) {
                            users.add(json.toObject(User::class.java))
                        }
                        callback(users)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun uploadImage(image: Bitmap, username: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageProfileRef = storageRef.child("images/$username.jpg")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageProfileRef.putBytes(data)
        uploadTask
            .addOnFailureListener { callback(null) }
            .addOnSuccessListener { taskSnapshot ->
                imageProfileRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
            }
    }
}