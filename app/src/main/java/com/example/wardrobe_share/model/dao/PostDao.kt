package com.example.wardrobe_share.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wardrobe_share.model.Post

@Dao
interface PostDao {
    @Query("SELECT * FROM post")
    fun getAll(): List<Post>
    @Query("SELECT * FROM post WHERE id = :id")
    fun getPostById(id: String): Post

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(vararg posts: Post)
    @Delete
    fun delete(post: Post)

}