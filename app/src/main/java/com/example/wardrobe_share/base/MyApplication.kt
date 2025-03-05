package com.example.wardrobe_share.base

import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.example.wardrobe_share.BuildConfig

class MyApplication : Application() {

    object Globals {
        var context: Context? = null
        var isCloudinaryInitialized = false // Flag to track if MediaManager is initialized
    }

    override fun onCreate() {
        super.onCreate()
        Globals.context = applicationContext

        // Initialize MediaManager only once when the application starts
        if (!Globals.isCloudinaryInitialized) {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
            )
            MediaManager.init(applicationContext, config)
            Globals.isCloudinaryInitialized = true
        }
    }
}
