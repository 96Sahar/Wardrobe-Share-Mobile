package com.example.wardrobe_share.model

import android.os.Looper
import androidx.core.os.HandlerCompat
import com.example.wardrobe_share.model.dao.AppLocalDb
import com.example.wardrobe_share.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

interface GetAllUsersListener{
    fun onGetAllStudents(users: List<User>)

}


class Model private constructor()
{
    val users : MutableList<User> = ArrayList()
    val posts : MutableList<Post> = ArrayList()
    private val database: AppLocalDbRepository = AppLocalDb.database;
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object{
        val shared = Model()
    }

    fun getAllUsers(listener: GetAllUsersListener): List<User> {
        executor.execute {
            val users = database.userDao().getAll()
            handler.post {
                listener.onGetAllStudents(users)
            }
        }
        return TODO("Provide the return value")
    }

    fun getUserById(id: String): User {
        return database.userDao().getUserById(id)
    }
    fun deleteUser(user: User) {
        database.userDao().delete(user)
    }
    fun insertUser(user: User) {
        database.userDao().insertUser(user)
    }

    fun getAllPosts(): List<Post> {
        return database.postDao().getAll()
    }
    fun getPostById(id: String): Post {
        return database.postDao().getPostById(id)
    }
    fun deletePost(post: Post) {
        database.postDao().delete(post)
    }


}