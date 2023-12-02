package com.group7.studdibuddi.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Data Access Objects here
// SQL command to kotlin functions
@Dao
interface SessionDatabaseDao {

    // Insert the new Entry into the database
    @Insert
    suspend fun insertEntry(newEntry: Session)

    // Select all the entries as flow datatype from the table
    @Query("SELECT * FROM session_table")
    fun getAllEntries(): Flow<List<Session>>

    // Delete the entry with id from the table
    @Query("DELETE FROM session_table WHERE sessionId = :key")
    suspend fun deleteEntry(key: Long)
}