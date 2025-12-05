package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SitterLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sitter_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (AppConfig.USE_FIREBASE) {
                // Firebase login
                btnLogin.isEnabled = false
                FirebaseRepository.signIn(
                    email = email,
                    password = password,
                    role = FirebaseRepository.UserRole.SITTER,
                    onSuccess = {
                        btnLogin.isEnabled = true
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, SitterHomeActivity::class.java))
                        finish()
                    },
                    onError = { error ->
                        btnLogin.isEnabled = true
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Fake data mode - check against fake sitters
                val fakeSitter = petSitters.find { it.email == email }
                if (fakeSitter != null && password == "1234") {
                    // Set current sitter info
                    currentSitter = fakeSitter
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SitterHomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid credentials. Try any sitter email with password: 1234", Toast.LENGTH_LONG).show()
                }
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SitterSignupActivity::class.java))
        }
    }

    companion object {
        // Current logged-in sitter (fake data mode)
        var currentSitter: PetSitter? = null
    }
}

