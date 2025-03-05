package com.example.wardrobe_share.api

import com.example.wardrobe_share.BuildConfig
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeminiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/models/gemini-1.5-pro:generateContent?key=" + BuildConfig.GEMINI_API_KEY)
    fun generateResponse(@Body request: GeminiRequest): Call<GeminiResponse>
}