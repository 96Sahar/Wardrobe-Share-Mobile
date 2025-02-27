package com.example.wardrobe_share.model.dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wardrobe_share.base.MyApplication
import com.example.wardrobe_share.model.Post
import com.example.wardrobe_share.model.User

@Database(entities = [Post::class, User::class], version = 1)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
}

object AppLocalDb {

    val database: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.context ?: throw IllegalStateException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "wardrobe.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}