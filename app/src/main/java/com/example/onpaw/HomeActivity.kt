package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        // Pet Sitter button - now navigates to sitter login
        findViewById<Button>(R.id.btn_pet_sitter).setOnClickListener {
            val intent = Intent(this, SitterLoginActivity::class.java)
            startActivity(intent)
        }

        // Pet Owner button
        findViewById<Button>(R.id.btn_pet_owner).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Emergency button
        findViewById<Button>(R.id.btn_emergency).setOnClickListener {
            val intent = Intent(this, EmergencyFindPetCareActivity::class.java)
            startActivity(intent)
        }
    }
}