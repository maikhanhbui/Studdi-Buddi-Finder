package com.group7.studdibuddi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.group7.studdibuddi.databinding.ActivityLoginBinding

class LoginActivity : ComponentActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var LOG_IN_BUTTON_TITLE: String
    private lateinit var NON_EMPTY_TITLE: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emailEntry = binding.emailEntry
        val passwordEntry = binding.passwordEntry

        LOG_IN_BUTTON_TITLE = getString(R.string.log_in_button)
        NON_EMPTY_TITLE = getString(R.string.entry_cant_be_empty)

        binding.loginButton.setOnClickListener{

            if (passwordEntry.text.isEmpty() or emailEntry.text.isEmpty()) {
                Toast.makeText(this, NON_EMPTY_TITLE, Toast.LENGTH_SHORT).show()
            }
            else {
                DatabaseUtil.signIn(
                    this,
                    emailEntry.text.toString(),
                    passwordEntry.text.toString()
                ) { success ->
                    if (success) {
                        Toast.makeText(this, LOG_IN_BUTTON_TITLE, Toast.LENGTH_SHORT).show()
                        //go back to home page
                        val intent: Intent?
                        intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        //authentication failure
                    }
                }
            }

        }
        binding.goRegisterButton.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
