package com.group7.studdibuddi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group7.studdibuddi.databinding.ActivityChatBinding
import com.group7.studdibuddi.Chat.ChatMessage
import com.group7.studdibuddi.Chat.ChatMessageAdapter
import com.group7.studdibuddi.session.SessionUtil

private lateinit var chatMessageAdapter: ChatMessageAdapter
private val messages = mutableListOf<ChatMessage>()
class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SessionUtil.selectedSession == null || DatabaseUtil.currentUser == null){
            Toast.makeText(this, "Session Error", Toast.LENGTH_SHORT).show()
            finish()
        }

        val curSession = SessionUtil.selectedSession!!

        // Initialize RecyclerView and Adapter
        chatMessageAdapter = ChatMessageAdapter(messages)

        binding.recyclerViewChat.adapter = chatMessageAdapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)

        // Setup Firebase Realtime Database reference
        val databaseReference = FirebaseDatabase.getInstance().getReference("chatMessages")

        // Read existing messages from Firebase
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (messageSnapshot in snapshot.children) {
                    val chatMessage = messageSnapshot.getValue(ChatMessage::class.java)
                    if (chatMessage != null) {
                        if (chatMessage.sessionID == curSession.sessionKey)
                            chatMessage.let { messages.add(it) }
                    }
                }
                chatMessageAdapter.notifyDataSetChanged()
                binding.recyclerViewChat.scrollToPosition(chatMessageAdapter.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Send button click listener
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val senderId = DatabaseUtil.currentUser!!.uid
                val timestamp = System.currentTimeMillis()

                // Create a new ChatMessage object
                val newMessage = ChatMessage(curSession.sessionKey, senderId, messageText, timestamp)

                // Push the new message to Firebase
                val newMessageRef = databaseReference.push()
                newMessageRef.setValue(newMessage)

                // Clear the input field
                binding.editTextMessage.text.clear()
            }
            else{
                Toast.makeText(this, "Message Cannot Be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}