package com.group7.studdibuddi.session

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Session::class], version = 1)
abstract class SessionDatabase: RoomDatabase(){
    abstract val entryDatabaseDao: SessionDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: SessionDatabase? = null

        fun getInstance(context: Context): SessionDatabase{
            synchronized(this){
                if(INSTANCE == null){
                    // If instance does not exist then build the database instance
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        SessionDatabase::class.java, "session_table").build()   // My entry table name here
                }
                // Otherwise return the instance
                return INSTANCE as SessionDatabase
            }
        }

    }
}