package com.example.wardrobe_share.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeminiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/models/gemini-1.5-pro:generateContent?key=AIzaSyClHf2ecppxi3P2kzsBB6f0Zarsml-DY18")
    fun generateResponse(@Body request: GeminiRequest): Call<GeminiResponse>
}