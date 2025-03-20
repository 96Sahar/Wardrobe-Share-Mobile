package com.example.wardrobe_share.api

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wardrobe_share.R

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object {
        const val USER_MESSAGE = 0
        const val BOT_MESSAGE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutId = when(viewType) {
            USER_MESSAGE -> R.layout.item_user_message
            BOT_MESSAGE -> R.layout.item_bot_message
            else -> throw IllegalArgumentException("Invalid view type")
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        val textMessage = if (viewType == USER_MESSAGE) {
            view.findViewById<TextView>(R.id.textUserMessage)
        } else {
            view.findViewById<TextView>(R.id.textBotMessage)
        }

        return ChatViewHolder(view, textMessage)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) USER_MESSAGE else BOT_MESSAGE
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class ChatViewHolder(itemView: View, private val textMessage: TextView) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: ChatMessage) {
            textMessage.text = message.text
        }
    }
}


