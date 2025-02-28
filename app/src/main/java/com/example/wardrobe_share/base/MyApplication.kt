package com.example.wardrobe_share.base

import android.app.Application
import android.content.Context

class MyApplication: Application() {


    object Globals {
        var context: Context? = null;
    }

    override fun onCreate() {
        super.onCreate()
        Globals.context = applicationContext
    }
}