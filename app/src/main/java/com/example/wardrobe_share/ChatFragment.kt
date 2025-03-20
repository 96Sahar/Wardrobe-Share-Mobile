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

    private val systemMessage = "You are a helpful assistant that only answers questions related to fashion, " +
            "second-hand shopping, and crafting better item descriptions. " +
            "Try to answer as briefly as possible. The website you're helping with is called Wardrobe-Share, " +
            "a second-hand clothing sharing website. Do not use emojis."

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_bot, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewChat)
        editTextUserInput = view.findViewById(R.id.editTextUserInput)
        buttonSend = view.findViewById(R.id.buttonSend)

        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = chatAdapter

        if (chatMessages.isEmpty()) {
            addMessage(
                "Hello! I'm your fashion assistant. I can help with second-hand shopping, styling tips, and crafting better item descriptions. How can I assist you today?",
                isUser = false
            )
        }

        buttonSend.setOnClickListener {
            val userInput = editTextUserInput.text.toString().trim()
            if (userInput.isNotEmpty()) {
                addMessage(userInput, true)
                editTextUserInput.text.clear()
                getChatbotResponse(userInput)
            }
        }

        return view
    }

    private fun addMessage(text: String, isUser: Boolean) {
        requireActivity().runOnUiThread {
            chatMessages.add(ChatMessage(text, isUser))
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            recyclerView.scrollToPosition(chatMessages.size - 1)
        }
    }

    private fun getChatbotResponse(userInput: String) {
        val systemContent = Content(
            role = "model",
            parts = listOf(Part(systemMessage))
        )

        val visibleHistory = chatMessages.map { message ->
            Content(
                role = if (message.isUser) "user" else "model",
                parts = listOf(Part(message.text))
            )
        }

        val contents = mutableListOf<Content>()
        contents.add(systemContent)
        contents.addAll(visibleHistory)

        val request = GeminiRequest(contents = contents)

        RetrofitClient.apiService.generateResponse(request).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (reply != null) {
                        addMessage(reply, false)
                    } else {
                        Log.e("ChatFragment", "Null response received")
                        resetBot()
                    }
                } else {
                    Log.e("ChatFragment", "Error: ${response.errorBody()?.string()}")
                    resetBot()

                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                Log.e("ChatFragment", "Failed: ${t.message}")
                resetBot()
            }
        })
    }

    private fun resetBot() {
        requireActivity().runOnUiThread {
            val lastUserMessage = chatMessages.lastOrNull { it.isUser }?.text

            chatMessages.clear()

            chatMessages.add(ChatMessage("I apologize, but I encountered an issue.", false))

            chatAdapter.notifyDataSetChanged()

            recyclerView.scrollToPosition(chatMessages.size - 1)

            Log.d("ChatFragment", "Chat has been reset due to an error")

            Handler(Looper.getMainLooper()).postDelayed({
                chatMessages.clear()

                chatMessages.add(ChatMessage("Let's start our conversation again, how can i help you?", false))

                chatAdapter.notifyDataSetChanged()

                Log.d("ChatFragment", "Chat has been cleared after delay")
            }, 2000)
        }
    }
}

