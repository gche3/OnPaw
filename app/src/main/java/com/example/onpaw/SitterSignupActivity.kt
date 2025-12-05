package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SitterSignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sitter_signup)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        btnSignUp.setOnClickListener {
            val name = etName.text?.toString()?.trim() ?: ""
            val email = etEmail.text?.toString()?.trim() ?: ""
            val phone = etPhone.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""
            val confirmPassword = etConfirmPassword.text?.toString() ?: ""

            // Validation
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 4) {
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (AppConfig.USE_FIREBASE) {
                // Firebase signup
                btnSignUp.isEnabled = false
                FirebaseRepository.signUp(
                    email = email,
                    password = password,
                    name = name,
                    phone = phone,
                    role = FirebaseRepository.UserRole.SITTER,
                    onSuccess = {
                        btnSignUp.isEnabled = true
                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, SitterHomeActivity::class.java))
                        finish()
                    },
                    onError = { error ->
                        btnSignUp.isEnabled = true
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Fake data mode - create new fake sitter
                val newSitter = PetSitter(
                    id = petSitters.size + 1,
                    name = name,
                    email = email,
                    phone = phone,
                    address = "",
                    latitude = 40.1020,
                    longitude = -88.2282,
                    rating = 5.0,
                    hourlyRate = 20.0,
                    bio = "New pet sitter ready to help!",
                    availability = "Flexible",
                    petTypes = listOf("Dog", "Cat"),
                    yearsExperience = 1
                )
                petSitters.add(newSitter)
                SitterLoginActivity.currentSitter = newSitter
                
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SitterHomeActivity::class.java))
                finish()
            }
        }

        tvLogin.setOnClickListener {
            finish() // Go back to login
        }
    }
}

