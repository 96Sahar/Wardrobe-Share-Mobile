package com.example.wardrobe_share

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wardrobe_share.api.ChatAdapter
import com.example.wardrobe_share.api.ChatMessage
import com.example.wardrobe_share.api.Content
import com.example.wardrobe_share.api.GeminiRequest
import com.example.wardrobe_share.api.GeminiResponse
import com.example.wardrobe_share.api.Part
import com.example.wardrobe_share.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextUserInput: EditText
    private lateinit var buttonSend: Button
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_bot, container, false)

        // Initialize UI components
        recyclerView = view.findViewById(R.id.recyclerViewChat)
        editTextUserInput = view.findViewById(R.id.editTextUserInput)
        buttonSend = view.findViewById(R.id.buttonSend)

        // Set up RecyclerView with Adapter
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = chatAdapter

        // Set the initial message for context
        val introMessage = "You are a helpful assistant that only answers questions related to fashion, " +
                "second-hand shopping, and crafting better item descriptions. " +
                "Try to answer as briefly as possible. The website you're helping with is called Wardrobe-Share, " +
                "a second-hand clothing sharing website. Do not use emojis."

        chatMessages.add(ChatMessage(introMessage, false)) // Add intro message

        // Handle send button click
        buttonSend.setOnClickListener {
            val userInput = editTextUserInput.text.toString().trim()
            if (userInput.isNotEmpty()) {
                addMessage(userInput, true)  // Add user message
                editTextUserInput.text.clear()
                getChatbotResponse(userInput) { response ->
                    addMessage(response, false)  // Add chatbot response
                }
            }
        }

        return view
    }

    // Function to add messages to RecyclerView
    private fun addMessage(text: String, isUser: Boolean) {
        requireActivity().runOnUiThread {
            chatMessages.add(ChatMessage(text, isUser))
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            recyclerView.scrollToPosition(chatMessages.size - 1)
        }
    }

    // Function to call Gemini API and get chatbot response
    private fun getChatbotResponse(userInput: String, callback: (String) -> Unit) {
        // Convert chat history into structured messages
        val history = chatMessages.map { message ->
            Content(
                role = if (message.isUser) "user" else "model", // Assign correct roles
                parts = listOf(Part(message.text))
            )
        }

        // Add new user message to the conversation
        val userMessage = Content(role = "user", parts = listOf(Part(userInput)))

        // Construct the request
        val request = GeminiRequest(contents = history + userMessage)

        RetrofitClient.apiService.generateResponse(request).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    callback(reply ?: "No response received.")
                } else {
                    callback("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                callback("Failed: ${t.message}")
            }
        })
    }

}
