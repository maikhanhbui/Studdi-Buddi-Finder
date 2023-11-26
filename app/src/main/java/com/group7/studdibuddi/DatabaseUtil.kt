package com.group7.studdibuddi

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

object DatabaseUtil {

    lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    var currentUser: FirebaseUser? = null


    val sfuLocation = LatLng(49.279, -122.918)

    fun initDatabase(){
        database = FirebaseDatabase.getInstance()
        auth = Firebase.auth
        currentUser = FirebaseAuth.getInstance().currentUser
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

                    // Email verification
                    val user = auth.currentUser
                    user!!.sendEmailVerification()
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                // Email sent, inform the user to check their email
                                Toast.makeText(activity, "Verification link sent to ${user.email}", Toast.LENGTH_SHORT).show()
                                callback(true)
                            } else {
                                // Failed to send verification email, show error message
                                Toast.makeText(activity, "Failed to send verification email, try again", Toast.LENGTH_SHORT).show()
                                callback(false)
                            }
                        }

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

    private fun sendEmailVerification(activity: Activity, user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email sent, inform the user to check their email
                    // Toast.makeText(activity, "Verification email sent to ${user.email}", Toast.LENGTH_SHORT).show()
                } else {
                    // Failed to send verification email, show error message
                    Toast.makeText(activity, "Failed to send verification email, try again", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun checkEmailVerification(activity: Activity) {
        // Check if the user's email is verified
        val user = FirebaseAuth.getInstance().currentUser
        if (user?.isEmailVerified == true) {
            // User's email is verified, proceed to the next screen or action
        } else {
            // Inform the user that their email is not verified yet
            Toast.makeText(activity, "Email not yet verified", Toast.LENGTH_SHORT).show()
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



                    // Email is successfully verified
                    val user = auth.currentUser
                    println("************************************************************")
                    if (user != null) {
                        println(user.isEmailVerified)
                    }
                    println("************************************************************")


                    if (user != null && user.isEmailVerified) {
                        println("************************************************************")
                        println(user.isEmailVerified)
                        println("************************************************************")

                        callback(true)
                    }
                    // Sign in successful, but email is not verified
                    else {
                        Toast.makeText(
                            activity,
                            "Please verify your email.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        callback(false)
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    val exception = task.exception
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

    fun isEmailDomainValid(email: String, domain: String): Boolean {
        val emailParts = email.split("@")
        if (emailParts.size == 2) {
            val userEmailDomain = emailParts[1]
            return userEmailDomain == domain
        }
        return false
    }


    fun createSession(activity: Activity,
                      sessionName: String,
                      location: Int,
                      latLng: LatLng,
                      courseId: String,
                      description: String,
                      callback: (Boolean) -> Unit) {
        if (currentUser == null){
            Toast.makeText(activity, "Login required", Toast.LENGTH_SHORT).show()
            callback(false)
        }
        val sessionDatabase = database.getReference("session")
        // push assigns the session with a random unique id
        val newSessionRef = sessionDatabase.push()

//        val session = mapOf(
//            "session_name" to sessionName,
//            "location" to mapOf(
//                "latitude" to latLng.latitude,
//                "longitude" to latLng.longitude
//            ),cd .
//            "owner_id" to currentUser!!.uid
//        )
        val newSession = Session(sessionName, courseId, location, latLng.latitude,latLng.longitude, description, currentUser!!.uid)
        newSessionRef.setValue(newSession).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "createSession:success$sessionName")
                Toast.makeText(
                    activity,
                    "Create Session Successfully",
                    Toast.LENGTH_SHORT,
                ).show()
                callback(true)
            } else {
                Log.e(TAG, "createSession:failure", task.exception)
                Toast.makeText(
                    activity,
                    "Create Session Error: ${task.exception?.message}",
                    Toast.LENGTH_SHORT,
                ).show()
                callback(false)
            }
        }
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