package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Simulate loading for 2 seconds, then navigate to FindSittersActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, FindSittersActivity::class.java)
            startActivity(intent)
            finish() // Close loading screen so back button doesn't return here
        }, 2000) // 2000ms = 2 seconds
    }
}

