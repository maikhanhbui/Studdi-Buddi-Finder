package com.group7.studdibuddi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.group7.studdibuddi.databinding.ActivityRegisterBinding

private lateinit var binding: ActivityRegisterBinding
class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emailEntry = binding.emailEntry
        val passwordEntry = binding.passwordEntry

        binding.registerButton.setOnClickListener{
            if (passwordEntry.text.isEmpty() or emailEntry.text.isEmpty()){
                Toast.makeText(this, "Entry cannot be empty", Toast.LENGTH_SHORT).show()
            }
            else {
                DatabaseUtil.createAccount(
                    this,
                    emailEntry.text.toString(),
                    passwordEntry.text.toString()
                ) { success ->
                    if (success) {
                        println("REGISTER IN SUCCESSFUL")
                        // Close the page on success
                        finish()
                    } else {
                        println("REGISTER IN FAILURE")
                    }
                }
            }

        }
    }
}
