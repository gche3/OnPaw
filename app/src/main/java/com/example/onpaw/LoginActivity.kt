package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Match IDs from login.xml
        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val signUpText = findViewById<TextView>(R.id.tvSignUp)
        val backButton = findViewById<ImageView>(R.id.login_back)

        // Login button logic
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            // Basic validation
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check credentials
            val findUser = userList.find { it.email == email && it.password == password }

            if (findUser != null) {
                user.name = findUser.name
                user.email = findUser.email
                user.phone = findUser.phone
                user.address = findUser.address
                user.latitude = findUser.latitude
                user.longitude = findUser.longitude
                user.password = findUser.password
                user.petList = findUser.petList
                user.isLoggedIn = true

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                // Navigate to HomeActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // “Don't have an account? Sign up here”
        signUpText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}
