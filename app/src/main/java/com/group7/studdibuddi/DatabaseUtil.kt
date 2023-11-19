package com.group7.studdibuddi

import android.app.Activity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

object Database {

    lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Connect to database, use local cache if failed
    fun establishConnection(activity: Activity){
        database = Firebase.database.reference
    }

    // Room database local access
    fun restoreLocalData(activity: Activity){

    }

}