package com.example.wardrobe_share.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wardrobe_share.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>
    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser( vararg users: User)
    @Delete
    fun delete(user: User)
}