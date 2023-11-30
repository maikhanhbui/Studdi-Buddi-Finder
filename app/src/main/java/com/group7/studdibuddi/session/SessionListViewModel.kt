package com.group7.studdibuddi.session

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData

// View Model where contains livedata pulled from the repository,
// any database actions can be defined here for easy access and the livedata can update accordingly for observer
class SessionViewModel(private val repository: SessionRepository): ViewModel(){

    var allSessionLiveData: LiveData<List<Session>> = repository.allEntries.asLiveData()

    fun insert(entry: Session) {
        repository.insert(entry)
        update()
    }

    fun deletePos(position: Int){
        val entryList = allSessionLiveData.value
        if (!entryList.isNullOrEmpty()){
            val id = entryList[position].sessionId
            Log.d("d", "deleted id: $id")
            repository.delete(id)
            update()
        }
        else{
            throw Exception("Delete error")
        }
    }

    fun update(){
        allSessionLiveData = repository.allEntries.asLiveData()
    }
}

// Build the ViewModel with the repository
class EntryViewModelFactory(private val repository: SessionRepository): ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(SessionViewModel::class.java)){
            return SessionViewModel(repository) as T
        }
        else{
            throw java.lang.Exception("ViewModel Error")
        }
    }
}