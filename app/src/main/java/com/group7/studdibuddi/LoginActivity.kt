package com.group7.studdibuddi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.group7.studdibuddi.databinding.ActivityLoginBinding

class LoginActivity : ComponentActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emailEntry = binding.emailEntry
        val passwordEntry = binding.passwordEntry

        binding.loginButton.setOnClickListener{
            if (FirebaseAuth.getInstance().currentUser != null){
                Toast.makeText(this,"Logged in", Toast.LENGTH_SHORT).show()
                //go back to home page
                var intent: Intent? = null
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            else {
                if (passwordEntry.text.isEmpty() or emailEntry.text.isEmpty()) {
                    Toast.makeText(this, "Entry cannot be empty", Toast.LENGTH_SHORT).show()
                } else {
                    DatabaseUtil.signIn(
                        this,
                        emailEntry.text.toString(),
                        passwordEntry.text.toString()
                    ) { success ->
                        if (success) {
                            Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
                            //go back to home page
                            var intent: Intent? = null
                            intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            //authentication failure
                        }
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
