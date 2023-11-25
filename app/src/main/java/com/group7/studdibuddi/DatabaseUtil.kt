package com.group7.studdibuddi

import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.android.gms.maps.model.LatLng

object DatabaseUtil {

    lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    val sfuLocation = LatLng(49.279, -122.918)

    fun initDatabase(){
        auth = Firebase.auth
    }
    fun createAccount(
        activity: Activity,
        email: String,
        password: String,
        callback: (Boolean) -> Unit)
    {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    callback(true)
                } else {
                    // If sign in fails, display a message to the user.
                    val exception = task.exception
                    Log.w(TAG, "createUserWithEmail:failure", exception)

                    // Check specific error cases and display relevant error messages
                    if (exception is FirebaseAuthUserCollisionException) {
                        // User with this email already exists
                        Toast.makeText(activity, "User with this email already exists", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthWeakPasswordException) {
                        // Password is too weak
                        Toast.makeText(activity, "Password is too weak", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        // Invalid email format
                        Toast.makeText(activity, "Invalid email format", Toast.LENGTH_SHORT).show()
                    } else {
                        // Other authentication failures
                        Toast.makeText(activity, "Authentication failed: " + exception?.message, Toast.LENGTH_SHORT).show()
                    }

                    callback(false)
                }
            }
    }

    fun signIn(
        activity: Activity,
        email: String,
        password: String,
        callback: (Boolean) -> Unit)
    {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    callback(true)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        activity,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    callback(false)
                }
            }
    }

    fun createSession(name: String, location: Int, latLng: LatLng, courseId: String, description: String){

    }

    fun loadSession(){

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