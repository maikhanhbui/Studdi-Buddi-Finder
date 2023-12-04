package com.group7.studdibuddi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.group7.studdibuddi.session.SessionUtil
import com.group7.studdibuddi.databinding.ActivityMySessionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MySessionActivity : ComponentActivity() {
    private lateinit var binding: ActivityMySessionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SessionUtil.selectedSession == null){
            Toast.makeText(this, "Session Error", Toast.LENGTH_SHORT).show()
            finish()
        }
        val curSession = SessionUtil.selectedSession!!

        // Draw the info
        binding.textViewSessionName.text = "Session Name: ${curSession.sessionName}"
        binding.textViewSessionLocation.text = "Location Number: ${curSession.location}"
        binding.textViewCourseId.text = "Course Number: ${curSession.courseId}"
        binding.textViewSessionDescription.text = "Description: ${curSession.description}"

        val timeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        binding.textViewStartTime.text = "Start Time: ${timeFormat.format(curSession.startTime)}"
        binding.textViewEndTime.text = "End Time: ${timeFormat.format(curSession.endTime)}"

        // Get Group Member names
        SessionUtil.joinedUser(curSession.usersJoined, curSession.ownerId) { usernames ->
            binding.textViewGroup.text = "Group Members: ${usernames.joinToString(", ")}"
        }

    }
}