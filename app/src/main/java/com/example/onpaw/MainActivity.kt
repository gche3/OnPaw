package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Settings icon click
        findViewById<ImageView>(R.id.settings_icon).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Pet Profiles button
        findViewById<Button>(R.id.btn_pet_profiles).setOnClickListener {
            val intent = Intent(this, PetProfilesActivity::class.java)
            startActivity(intent)
        }

        // Find Sitters button
        findViewById<Button>(R.id.btn_find_sitters).setOnClickListener {
            val intent = Intent(this, LoadingActivity::class.java)
            startActivity(intent)
        }

        // My Bookings button (placeholder for now)
        findViewById<Button>(R.id.btn_my_bookings).setOnClickListener {
            Toast.makeText(this, "My Bookings - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }
}