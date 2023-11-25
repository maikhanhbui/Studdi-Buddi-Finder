package com.group7.studdibuddi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.group7.studdibuddi.databinding.ActivityRegisterBinding

class RegisterActivity : ComponentActivity() {

    private lateinit var binding: ActivityRegisterBinding
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
                        Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        // Error messages are displayed
                    }
                }
            }

        }
    }
}
