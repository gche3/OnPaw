package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnFindPetCare = findViewById<Button>(R.id.btnFindPetCare)
        val btnEmergencyPetCare = findViewById<Button>(R.id.btnEmergencyPetCare)
        val btnMessage = findViewById<Button>(R.id.btnMessage)

        btnFindPetCare.setOnClickListener {
            val intent = Intent(this, FindPetCareActivity::class.java)
            startActivity(intent)
        }

        btnEmergencyPetCare.setOnClickListener {
            val intent = Intent(this, EmergencyFindPetCareActivity::class.java)
            startActivity(intent)
        }
        btnMessage.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }
}
