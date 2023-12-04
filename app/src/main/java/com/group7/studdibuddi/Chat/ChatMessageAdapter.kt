package com.group7.studdibuddi.Chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group7.studdibuddi.DatabaseUtil
import com.group7.studdibuddi.R
import com.group7.studdibuddi.Util

class ChatMessageAdapter (private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.message
        DatabaseUtil.findUserName(message.senderId){userName->
            if (userName != null) {
                holder.senderTextView.text = userName
            } else {
                holder.senderTextView.text = "Unknown User"
            }
        }
        holder.timeTextView.text = Util.timeStampToTimeString(message.timestamp)
    }

    override fun getItemCount(): Int = messages.size
}