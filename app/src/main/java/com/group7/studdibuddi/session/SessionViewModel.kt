package com.group7.studdibuddi.session

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// View Model where contains livedata pulled from the repository,
// any database actions can be defined here for easy access and the livedata can update accordingly for observer
class SessionViewModel : ViewModel() {

    private val _allSessionLiveData = MutableLiveData<List<Session>>()
    val allSessionLiveData: LiveData<List<Session>>
        get() = _allSessionLiveData

    // Step 4: Fetch and Update Data
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val sessionDataReference = firebaseDatabase.getReference("session")

    init {
        // Set up a ValueEventListener to listen for changes in Firebase
        sessionDataReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sessions = snapshot.children.mapNotNull { it.getValue(Session::class.java) }
                _allSessionLiveData.value = sessions
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Function to update data in Firebase
    fun updateData(newSessions: List<Session>) {
        // Update data in Firebase
        // Here, you might want to update each session individually or handle the list as per your data structure
        // For simplicity, this example assumes you're updating the entire list
        sessionDataReference.setValue(newSessions)
    }
}

class SessionViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

