package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Auto-skip login if already logged in
        if (user.isLoggedIn) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Match IDs from login.xml
        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val signUpText = findViewById<TextView>(R.id.tvSignUp)

        // Login button logic
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            // Basic validation
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Case: no account saved yet
            if (user.email.isBlank() || user.password.isBlank()) {
                Toast.makeText(this, "No account found. Please sign up first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check credentials
            val emailMatches = email.equals(user.email, ignoreCase = true)
            val passwordMatches = password == user.password

            if (emailMatches && passwordMatches) {
                user.isLoggedIn = true

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                // Navigate to HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // “Don't have an account? Sign up here”
        signUpText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
