package com.group7.studdibuddi

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

object DatabaseUtil {

    lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    fun userLogin(email: String, passWord: String): Boolean{
        // TODO: given email, password log the user in, NOTE: register page required first to test it
        // TODO: return true is login successful
        return true
    }

    // Room database local access
    fun restoreLocalData(activity: Activity){

    }

    fun isNetworkAvailable(): Boolean {
//        val connectivityManager =
//            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetworkInfo = connectivityManager.activeNetworkInfo
//        return activeNetworkInfo != null
        return true
    }


}