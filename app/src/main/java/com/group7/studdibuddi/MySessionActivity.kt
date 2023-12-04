package com.group7.studdibuddi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.database.FirebaseDatabase
import com.group7.studdibuddi.session.SessionUtil
import com.group7.studdibuddi.databinding.ActivityMySessionBinding
import com.group7.studdibuddi.session.Session

class MySessionActivity : ComponentActivity() {
    private lateinit var binding: ActivityMySessionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (SessionUtil.selectedSession == null || DatabaseUtil.currentUser == null){
            Toast.makeText(this, getString(R.string.session_error), Toast.LENGTH_SHORT).show()
            Log.d("database", "Chat session data error")
            finish()
        }

        val curSession = SessionUtil.selectedSession!!

        // Draw the info
        binding.textViewSessionName.text = "${getString(R.string.session_name_2)}: ${curSession.sessionName}"
        binding.textViewSessionLocation.text = "${getString(R.string.location_number)}: ${Util.getLocationStringFromInt(curSession.location)}"
        binding.textViewCourseId.text = "${getString(R.string.course_number)}: ${curSession.courseId}"
        binding.textViewSessionDescription.text = "${getString(R.string.description_2)}: ${curSession.description}"

        binding.textViewStartTime.text = "${getString(R.string.start_time)} ${Util.timeStampToTimeString(curSession.startTime)}"
        binding.textViewEndTime.text = "${getString(R.string.end_time)} ${Util.timeStampToTimeString(curSession.endTime)}"

        binding.buttonChat.setOnClickListener{
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // Get Group Member names
        SessionUtil.joinedUser(curSession.usersJoined, curSession.ownerId) { usernames ->
            binding.textViewGroup.text = "${getString(R.string.group_members)}: ${usernames.joinToString(", ")}"
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
        Toast.makeText(this, getString(R.string.successfully_left_group), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteSession(curSession: Session) {
        val sessionsRef = FirebaseDatabase.getInstance().getReference("session")
        sessionsRef.child(curSession.sessionKey).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "${getString(R.string.delete_unsuccessfully)}: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}