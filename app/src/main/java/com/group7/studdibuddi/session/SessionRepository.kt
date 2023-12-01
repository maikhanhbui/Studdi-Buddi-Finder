package com.group7.studdibuddi.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// Repository with contains the data from the data base as flow list,
// and can execute database operation (DAO)
class SessionRepository(private val sessionDatabaseDao: SessionDatabaseDao){

    val allEntries: Flow<List<Session>> = sessionDatabaseDao.getAllEntries()

    // Use coroutine to insert the given entry, so it will not block the main thread
    fun insert(newEntry: Session){
        CoroutineScope(Dispatchers.IO).launch {
            sessionDatabaseDao.insertEntry(newEntry)
        }
    }

    // Use coroutine to delete the entry with that id, so it will not block the main thread
    fun delete(id: Long){
        CoroutineScope(Dispatchers.IO).launch {
            sessionDatabaseDao.deleteEntry(id)
        }
    }

}