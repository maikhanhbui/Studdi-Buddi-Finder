package com.group7.studdibuddi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.group7.studdibuddi.session.BaseActivity
import java.io.File
import java.io.FileOutputStream

class UserProfileActivity : BaseActivity() {
    private lateinit var imageView: ImageView
    private lateinit var nameView: TextView
    private lateinit var emailView: TextView
    private lateinit var phoneView: TextView
    private lateinit var genderView: RadioGroup
    private lateinit var courseView: TextView
    private lateinit var majorView: TextView

    private lateinit var profilePictureUri: Uri
    private lateinit var tempProfilePictureUri: Uri
    private lateinit var pickedProfilePictureUri: Uri
    private lateinit var profilePicture: File
    private lateinit var tempProfilePicture: File
    private lateinit var pickedProfilePicture: File
    private val profilePictureName = "pfp.jpg"
    private val tempProfilePictureName = "temp_pfp.jpg"
    private val pickedProfilePictureName = "picked_pfp.jpg"

    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var profilePictureViewModel: UserPFPViewModel

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var changeButton: Button
    private lateinit var logOutButton: Button

    private lateinit var LOG_OUT_BUTTON_TITLE: String
    private lateinit var NOT_LOGGED_IN_TITLE: String
    private lateinit var SELECT_IMAGE_TITLE: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        title = getString(R.string.profile)

        LOG_OUT_BUTTON_TITLE = getString(R.string.log_out_button)
        NOT_LOGGED_IN_TITLE = getString(R.string.not_logged_in)
        SELECT_IMAGE_TITLE = getString(R.string.select_profile_image)

        //Find views
        imageView = findViewById(R.id.profile_photo)
        nameView = findViewById(R.id.your_name)
        emailView = findViewById(R.id.your_email)
        phoneView = findViewById(R.id.your_number)
        genderView = findViewById(R.id.gender)
        courseView = findViewById(R.id.courses)
        majorView = findViewById(R.id.major)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
        changeButton = findViewById(R.id.change_button)
        logOutButton = findViewById(R.id.logout_activity_button)

        Util.checkPermissions(this)

        //Get uri for profile picture
        profilePicture = File(getExternalFilesDir(null), profilePictureName)
        profilePictureUri = FileProvider.getUriForFile(this, "com.group7.studdibuddi", profilePicture)
        //Get uri for temp profile picture
        tempProfilePicture = File(getExternalFilesDir(null), tempProfilePictureName)
        tempProfilePictureUri = FileProvider.getUriForFile(this, "com.group7.studdibuddi", tempProfilePicture)

        pickedProfilePicture = File(getExternalFilesDir(null), pickedProfilePictureName)
        pickedProfilePictureUri = FileProvider.getUriForFile(this, "com.group7.studdibuddi", tempProfilePicture)

        //Set cameraResult to temporary profile picture
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap = Util.getBitmap(this, tempProfilePictureUri)
                profilePictureViewModel.userImage.value = bitmap
                MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, tempProfilePictureName, null)
                if (pickedProfilePicture.exists()) {
                    pickedProfilePicture.delete()
                }
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                //save Uri picked to tempProfilePicture
                pickedProfilePictureUri = result.data?.data!!
                val bitmap = Util.getBitmap(this, pickedProfilePictureUri)
                val fOut = FileOutputStream(pickedProfilePicture)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
                fOut.flush()
                fOut.close()
                //set imageview
                profilePictureViewModel.userImage.value = Util.getBitmap(this, pickedProfilePictureUri)
                if (tempProfilePicture.exists()) {
                    tempProfilePicture.delete()
                }
            }
        }

        //View
        profilePictureViewModel = ViewModelProvider(this)[UserPFPViewModel::class.java]
        profilePictureViewModel.userImage.observe(this) { it ->
            imageView.setImageBitmap(it)
        }

        //Load profile picture
        if(profilePicture.exists()) {
            imageView.setImageBitmap(Util.getBitmap(this, profilePictureUri))
        }

        //loads saved data
        loadProfile()

        saveButton.setOnClickListener {
            if (tempProfilePicture.exists()) {
                tempProfilePicture.renameTo(profilePicture)
            }
            if (pickedProfilePicture.exists()) {
                pickedProfilePicture.renameTo(profilePicture)
            }
            // Disable the button in case user repeat request
            saveButton.isEnabled = false
            DatabaseUtil.userProfileUpdate(
                this,
                    nameView.text.toString(),
                emailView.text.toString(),
                phoneView.text.toString(),
                this.getCheckedRadioButtonPosition(genderView),
                courseView.text.toString(),
                majorView.text.toString()) { success ->
                if (success) {
                    finish()
                } else {
                    //displays error messages
                }
            }
        }
        cancelButton.setOnClickListener {
            finish()
        }
        changeButton.setOnClickListener {
            val items = resources.getStringArray(R.array.change_profile_picture)
            val alertDialogBuilder = AlertDialog.Builder(this)
            var intent: Intent
            alertDialogBuilder.setTitle(SELECT_IMAGE_TITLE)
                .setItems(items) { _, index -> when(index)
                    {
                        0 -> {
                            intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempProfilePictureUri)
                            cameraResult.launch(intent)
                        }
                        1 -> {
                            intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                            galleryResult.launch(intent)
                        }
                    }
                }
            alertDialogBuilder.show()
        }
        logOutButton.setOnClickListener{
            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().signOut()
                DatabaseUtil.currentUser = null
                DatabaseUtil.currentUserProfile = null
                Toast.makeText(this,LOG_OUT_BUTTON_TITLE, Toast.LENGTH_SHORT).show()
                val intent: Intent?
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this,NOT_LOGGED_IN_TITLE, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProfile() {
        // Load the user's profile from database
        if(DatabaseUtil.currentUserProfile != null){
            nameView.text = DatabaseUtil.currentUserProfile!!.userName
            emailView.text = DatabaseUtil.currentUserProfile!!.personalEmail
            phoneView.text = DatabaseUtil.currentUserProfile!!.phoneNumber
            genderView.check(DatabaseUtil.currentUserProfile!!.gender)
            val genderIndex = DatabaseUtil.currentUserProfile!!.gender
            if (genderIndex >= 0 && genderIndex < genderView.childCount) {
                val radioButton = genderView.getChildAt(genderIndex) as RadioButton
                radioButton.isChecked = true
            }
            courseView.text = DatabaseUtil.currentUserProfile!!.coursesEnrolled
            majorView.text = DatabaseUtil.currentUserProfile!!.major
        }
    }

    private fun getCheckedRadioButtonPosition(radioGroup: RadioGroup): Int {
        val checkedRadioButtonId = radioGroup.checkedRadioButtonId

        if (checkedRadioButtonId != -1) {
            val checkedRadioButton = findViewById<RadioButton>(checkedRadioButtonId)
            return radioGroup.indexOfChild(checkedRadioButton)
        }

        return -1
    }
}
