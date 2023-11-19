package com.group7.studdibuddi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.group7.studdibuddi.databinding.ActivityLoginBinding

private lateinit var binding: ActivityLoginBinding
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emailEntry = binding.emailEntry
        val passwordEntry = binding.passwordEntry

        binding.loginButton.setOnClickListener{
            if (passwordEntry.text.isEmpty() or emailEntry.text.isEmpty()){
                Toast.makeText(this, "Entry cannot be empty", Toast.LENGTH_SHORT).show()
            }

            DatabaseUtil.signIn(this, emailEntry.text.toString(), passwordEntry.text.toString()) { success ->
                if (success) {
                    println("SIGN IN SUCCESSFUL")
                }
                else {
                    println("SIGN IN FAILURE")
                }
            }

        }
    }
}
