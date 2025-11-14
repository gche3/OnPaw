package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)


        findViewById<EditText>(R.id.settings_name).setText(user.name)
        findViewById<EditText>(R.id.settings_email).setText(user.email)
        findViewById<EditText>(R.id.settings_phone).setText(user.phone)
        findViewById<EditText>(R.id.settings_address).setText(user.address)

        findViewById<Button>(R.id.settings_reset).setOnClickListener {
            findViewById<EditText>(R.id.settings_name).setText(user.name)
            findViewById<EditText>(R.id.settings_email).setText(user.email)
            findViewById<EditText>(R.id.settings_phone).setText(user.phone)
            findViewById<EditText>(R.id.settings_address).setText(user.address)
        }

        findViewById<Button>(R.id.settings_save).setOnClickListener {
            val nameField = findViewById<EditText>(R.id.settings_name)
            val emailField = findViewById<EditText>(R.id.settings_email)
            val phoneField = findViewById<EditText>(R.id.settings_phone)
            val addressField = findViewById<EditText>(R.id.settings_address)

            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val address = addressField.text.toString().trim()

            val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            val isPhoneValid = android.util.Patterns.PHONE.matcher(phone).matches()

            when {
                name.isEmpty() -> {
                    nameField.error = "Name cannot be empty"
                }
                !isEmailValid -> {
                    emailField.error = "Invalid email address"
                }
                !isPhoneValid -> {
                    phoneField.error = "Invalid phone number"
                }
                else -> {
                    user.name = name
                    user.email = email
                    user.phone = phone
                    user.address = address

                    AlertDialog.Builder(this)
                        .setTitle("Saved")
                        .setMessage("Your settings have been updated successfully.")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }

        findViewById<Button>(R.id.settings_logout).setOnClickListener {
            // logout properly later
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.settings_delete).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirm Deletion")
            builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")

            builder.setPositiveButton("Yes") { dialog, _ ->
                petList.clear()
                user.delete()
                // actually implement later when we have a database

                dialog.dismiss()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()
        }

        findViewById<Button>(R.id.settings_back).setOnClickListener {
            finish()
        }
    }
}