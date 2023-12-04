package com.group7.studdibuddi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.firebase.database.FirebaseDatabase
import com.group7.studdibuddi.session.SessionUtil
import com.group7.studdibuddi.databinding.ActivityMySessionBinding
import com.group7.studdibuddi.session.Session
import java.text.SimpleDateFormat
import java.util.Locale

class MySessionActivity : ComponentActivity() {
    private lateinit var binding: ActivityMySessionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (SessionUtil.selectedSession == null || DatabaseUtil.currentUser == null){
            Toast.makeText(this, "Session Error", Toast.LENGTH_SHORT).show()
            Log.d("database", "Chat session data error")
            finish()
        }
        val curSession = SessionUtil.selectedSession!!

        // Draw the info
        binding.textViewSessionName.text = "Session Name: ${curSession.sessionName}"
        binding.textViewSessionLocation.text = "Location Number: ${curSession.location}"
        binding.textViewCourseId.text = "Course Number: ${curSession.courseId}"
        binding.textViewSessionDescription.text = "Description: ${curSession.description}"

        binding.textViewStartTime.text = "Start Time: ${Util.timeStampToTimeString(curSession.startTime)}"
        binding.textViewEndTime.text = "End Time: ${Util.timeStampToTimeString(curSession.endTime)}"

        binding.buttonChat.setOnClickListener{
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // Get Group Member names
        SessionUtil.joinedUser(curSession.usersJoined, curSession.ownerId) { usernames ->
            binding.textViewGroup.text = "Group Members: ${usernames.joinToString(", ")}"
        }

        // Check if the current user is the owner
        val isCurrentUserOwner = DatabaseUtil.currentUser?.uid == curSession.ownerId

        if (!isCurrentUserOwner) {
            // Show "Leave" button
            showLeaveButton(curSession)
        } else {
            // Show "Delete" button
            showDeleteButton(curSession)
        }

    }

    private fun showLeaveButton(curSession: Session) {
        val leaveButton = findViewById<Button>(R.id.buttonLeaveSession)
        leaveButton.visibility = View.VISIBLE
        leaveButton.setOnClickListener {
            leaveGroup(curSession)
        }
    }

    private fun showDeleteButton(curSession: Session) {
        val deleteButton = findViewById<Button>(R.id.buttonDeleteSession)
        deleteButton.visibility = View.VISIBLE
        deleteButton.setOnClickListener {
            deleteSession(curSession)
        }
    }

    private fun leaveGroup(curSession: Session) {
        val sessionsRef = FirebaseDatabase.getInstance().getReference("session")
        curSession.usersJoined.remove(DatabaseUtil.currentUser?.uid)
        sessionsRef.child(curSession.sessionKey).setValue(curSession)
        Toast.makeText(this, "Successfully left group!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteSession(curSession: Session) {
        val sessionsRef = FirebaseDatabase.getInstance().getReference("session")
        sessionsRef.child(curSession.sessionKey).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully deleted session!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete session: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}