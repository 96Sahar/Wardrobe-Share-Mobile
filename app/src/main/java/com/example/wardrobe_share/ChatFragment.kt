package com.example.wardrobe_share

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    // Store the system message separately
    private val systemMessage = "You are a helpful assistant that only answers questions related to fashion, " +
            "second-hand shopping, and crafting better item descriptions. " +
            "Try to answer as briefly as possible. The website you're helping with is called Wardrobe-Share, " +
            "a second-hand clothing sharing website. Do not use emojis."

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

        // No longer adding the system message to chatMessages
        // so it won't appear in the UI

        // Handle send button click
        buttonSend.setOnClickListener {
            val userInput = editTextUserInput.text.toString().trim()
            if (userInput.isNotEmpty()) {
                addMessage(userInput, true)  // Add user message
                editTextUserInput.text.clear()
                getChatbotResponse(userInput)
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
    private fun getChatbotResponse(userInput: String) {
        // Create a system message content object
        val systemContent = Content(
            role = "model",
            parts = listOf(Part(systemMessage))
        )

        // Convert visible chat history into structured messages
        val visibleHistory = chatMessages.map { message ->
            Content(
                role = if (message.isUser) "user" else "model",
                parts = listOf(Part(message.text))
            )
        }

        // Construct the request with system message first, then visible history
        val contents = mutableListOf<Content>()
        contents.add(systemContent) // Add system message first
        contents.addAll(visibleHistory) // Then add visible history

        val request = GeminiRequest(contents = contents)

        RetrofitClient.apiService.generateResponse(request).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (reply != null) {
                        addMessage(reply, false)
                    } else {
                        // Handle null response by resetting the chat
                        Log.e("ChatFragment", "Null response received")
                        resetBot()
                    }
                } else {
                    // Handle unsuccessful response by resetting the chat
                    Log.e("ChatFragment", "Error: ${response.errorBody()?.string()}")
                    resetBot()

                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                // Handle failure by resetting the chat
                Log.e("ChatFragment", "Failed: ${t.message}")
                resetBot()
            }
        })
    }

    private fun resetBot() {
        requireActivity().runOnUiThread {
            // First, save the user's last message if there is one
            val lastUserMessage = chatMessages.lastOrNull { it.isUser }?.text

            // Clear all messages except keep the last user message if needed
            chatMessages.clear()

            // Add the apology message
            chatMessages.add(ChatMessage("I apologize, but I encountered an issue.", false))

            // Notify adapter of changes
            chatAdapter.notifyDataSetChanged()

            // Scroll to the new message
            recyclerView.scrollToPosition(chatMessages.size - 1)

            // Log the error
            Log.d("ChatFragment", "Chat has been reset due to an error")

            // Wait for 2 seconds, then clear the chat
            Handler(Looper.getMainLooper()).postDelayed({
                // Clear all messages
                chatMessages.clear()

                chatMessages.add(ChatMessage("Let's start our conversation again, how can i help you?", false))

                // Notify adapter of changes
                chatAdapter.notifyDataSetChanged()

                // Log that the chat was cleared after delay
                Log.d("ChatFragment", "Chat has been cleared after delay")
            }, 2000) // 2000 milliseconds = 2 seconds
        }
    }
}

