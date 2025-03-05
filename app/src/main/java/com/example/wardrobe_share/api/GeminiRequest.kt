package com.example.wardrobe_share.api

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val role: String, // <-- Add role field: "user" or "model"
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)
