package com.group7.studdibuddi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.group7.studdibuddi.databinding.ActivityRegisterBinding

class RegisterActivity : ComponentActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var NON_EMPTY_TITLE: String
    private lateinit var EMAIL_DOMAIN_TITLE: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emailEntry = binding.emailEntry
        val passwordEntry = binding.passwordEntry
        NON_EMPTY_TITLE = getString(R.string.entry_cant_be_empty)
        EMAIL_DOMAIN_TITLE = getString(R.string.email_domain_must_be_sfu)

        binding.registerButton.setOnClickListener{
            val userEmailEntry = emailEntry.text.toString()
            val requiredEmailDomain = "sfu.ca"

            if (passwordEntry.text.isEmpty() or emailEntry.text.isEmpty()){
                Toast.makeText(this, NON_EMPTY_TITLE, Toast.LENGTH_SHORT).show()
            }
            else if (!DatabaseUtil.isEmailDomainValid(userEmailEntry, requiredEmailDomain)) {
                Toast.makeText(this, EMAIL_DOMAIN_TITLE, Toast.LENGTH_SHORT).show()
            }
            else {
                DatabaseUtil.createAccount(
                    this,
                    emailEntry.text.toString(),
                    passwordEntry.text.toString()
                ) { success ->
                    if (success) {
                        finish()
                    } else { }
                }
            }

        }
    }
}
