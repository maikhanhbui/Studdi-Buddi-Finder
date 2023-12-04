package com.group7.studdibuddi

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.group7.studdibuddi.session.Session
import com.group7.studdibuddi.userProfile.User

object DatabaseUtil {

    lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    var currentUser: FirebaseUser? = null

    val sfuLocation = LatLng(49.279, -122.918)

    // Strings
    private val toastVerificationLinkSent by lazy { R.string.toast_verification_link_sent }
    private val toastFailedVerificationEmail by lazy { R.string.toast_failed_verification_email }
    private val toastUserExists by lazy { R.string.toast_user_exists }
    private val toastWeakPassword by lazy { R.string.toast_weak_password }
    private val toastInvalidEmailFormat by lazy { R.string.toast_invalid_email_format }
    private val toastPleaseVerifyEmail by lazy { R.string.toast_please_verify_email }
    private val toastAuthenticationFailed by lazy { R.string.toast_authentication_failed }
    private val toastLoginRequired by lazy { R.string.toast_login_required }
    private val toastCreateSessionSuccess by lazy { R.string.toast_create_session_success }
    private val toastCreateSessionError by lazy { R.string.toast_create_session_error }

    private val toastUpdateProfileSuccess by lazy { R.string.toast_update_profile_success }
    private val toastUpdateProfileError by lazy { R.string.toast_update_profile_error }

    fun initDatabase(){
        database = FirebaseDatabase.getInstance()
        auth = Firebase.auth
        currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            initUser()
        }
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
                                val message = activity.getString(toastVerificationLinkSent)
                                Toast.makeText(activity, "$message ${user.email}", Toast.LENGTH_SHORT).show()
                                callback(true)
                            } else {
                                // Failed to send verification email, show error message
                                Toast.makeText(activity, toastFailedVerificationEmail, Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(activity, toastUserExists, Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthWeakPasswordException) {
                        // Password is too weak
                        Toast.makeText(activity, toastWeakPassword, Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        // Invalid email format
                        Toast.makeText(activity, toastInvalidEmailFormat, Toast.LENGTH_SHORT).show()
                    } else {
                        // Other authentication failures
                        val message = activity.getString(toastAuthenticationFailed)
                        Toast.makeText(activity, "$message: " + exception?.message, Toast.LENGTH_SHORT).show()
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
                            toastPleaseVerifyEmail,
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
                        toastAuthenticationFailed,
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
                      isPublic: Boolean,
                      startTime: Long,
                      endTime: Long,
                      callback: (String?) -> Unit) {

        if (currentUser == null){
            Toast.makeText(activity, toastLoginRequired, Toast.LENGTH_SHORT).show()
            callback(null)
        }
        val sessionDatabase = database.getReference("session")
        // push assigns the session with a random unique id
        val newSessionRef = sessionDatabase.push()

        val newSession = Session()
        newSession.sessionKey = newSessionRef.key.toString()
        newSession.sessionName = sessionName
        newSession.courseId = courseId
        newSession.location = location
        newSession.latitude = latLng.latitude
        newSession.longitude = latLng.longitude
        newSession.description = description
        newSession.ownerId = currentUser!!.uid
        newSession.isPublic = isPublic
        newSession.startTime = startTime
        newSession.endTime = endTime
        newSessionRef.setValue(newSession).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "createSession:success$sessionName")
                Toast.makeText(
                    activity,
                    toastCreateSessionSuccess,
                    Toast.LENGTH_SHORT,
                ).show()
                callback(newSessionRef.key.toString())
            } else {
                Log.e(TAG, "createSession:failure", task.exception)
                val message = activity.getString(toastCreateSessionError)
                Toast.makeText(
                    activity,
                    "$message ${task.exception?.message}",
                    Toast.LENGTH_SHORT,
                ).show()
                callback(null)
            }
        }
    }

    fun loadSession(){

    }

    // Room database local access
    fun restoreLocalData(activity: Activity){

    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            return networkCapabilities?.let {
                it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            } ?: false
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo?.isConnectedOrConnecting == true
        }
    }

    var currentUserProfile: User? = null

    private fun initUser(){
        val userId = currentUser!!.uid
        val currentUserRef = database.getReference("/users/$userId")

        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Check if the dataSnapshot exists and has children
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    // Retrieve the user data
                    val user = dataSnapshot.getValue(User::class.java)
                    // Use the user object as needed
                    if (user != null) {
                        currentUserProfile = user
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        }
        currentUserRef.addListenerForSingleValueEvent(userListener)
    }

    fun userProfileUpdate(activity: Activity,
                          name: String,
                          email: String,
                          number: String,
                          gender: Int,
                          coursesEnrolled: String,
                          major: String,
                          callback: (Boolean) -> Unit) {

        if (currentUser == null){
            Toast.makeText(activity, toastLoginRequired, Toast.LENGTH_SHORT).show()
            callback(false)
        }
        val userId = currentUser!!.uid
        val currentUserRef = database.getReference("/users/$userId")

        val updatedUserData = User()
        updatedUserData.userId = userId
        updatedUserData.userName = name
        updatedUserData.personalEmail = email
        updatedUserData.phoneNumber = number
        updatedUserData.gender = gender
        updatedUserData.coursesEnrolled = coursesEnrolled
        updatedUserData.major = major

        currentUserRef.setValue(updatedUserData).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Update user profile successfully")
                Toast.makeText(
                    activity,
                    toastUpdateProfileSuccess,
                    Toast.LENGTH_SHORT,
                ).show()
                currentUserProfile = updatedUserData
                callback(true)
            } else {
                Log.e(TAG, "createSession:failure", task.exception)
                Toast.makeText(
                    activity,
                    "$toastUpdateProfileError ${task.exception?.message}",
                    Toast.LENGTH_SHORT,
                ).show()
                callback(false)
            }
        }
    }

    fun findUserName(userId: String, callback: (String?) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users")
        val userQuery = userRef.child(userId)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("userName").getValue(String::class.java)
                // Add the username to the list
                callback(username)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun uploadImageToFirebaseStorage(imageUri: Uri?, sessionId: String, callback: (Boolean) -> Unit) {
        imageUri?.let {
            val storageReference = FirebaseStorage.getInstance().reference.child("images")
            val imageRef = storageReference.child("$sessionId/imageName.jpg")

            val uploadTask = imageRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val imageUrl = downloadUri.toString()
                    updateSessionImage(sessionId, imageUrl)
                    callback(true)
                } else {
                    callback(false)
                    // Handle the error
                }
            }
        }
    }
    private fun updateSessionImage(sessionId: String, imageUrl: String) {
        val sessionRef = database.getReference("/session/$sessionId/imageURL")

        sessionRef.setValue(imageUrl)
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpdateError", "Error updating session image URL: $e")
            }
    }




}