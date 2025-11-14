package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        // Match IDs from signup.xml
        val emailInput = findViewById<EditText>(R.id.etEmailSignup)
        val phoneInput = findViewById<EditText>(R.id.etPhoneNumber)
        val displayNameInput = findViewById<EditText>(R.id.etDisplayName)
        val passwordInput = findViewById<EditText>(R.id.etPasswordSignup)
        val confirmPasswordInput = findViewById<EditText>(R.id.etConfirmPassword)
        val createAccountButton = findViewById<Button>(R.id.btnCreateAccount)
        val loginHereText = findViewById<TextView>(R.id.tvLoginHere)

        createAccountButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val displayName = displayNameInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Basic validation
            if (email.isEmpty() || phone.isEmpty() || displayName.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save into global user (DataModels.kt)
            user.name = displayName
            user.email = email
            user.phone = phone
            // keep existing default address/coords unless you want to change them
            user.password = password
            user.isLoggedIn = true

            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()

            // Go to HomeActivity as the main screen
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // "Already have an account? Log in here"
        loginHereText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
