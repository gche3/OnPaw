package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class IndividualPetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_pet_profile_activity)

        val profileIdx = intent.getIntExtra("profileIdx", -1)
        if (profileIdx !in petList.indices) {
            finish()
            return
        }

        val profile = petList[profileIdx]

        findViewById<EditText>(R.id.pet_name).setText(profile.name)
        findViewById<EditText>(R.id.pet_species).setText(profile.species)
        findViewById<EditText>(R.id.pet_age).setText(profile.age.toString())
        findViewById<EditText>(R.id.pet_note).setText(profile.note)

        findViewById<Button>(R.id.pet_reset_button).setOnClickListener {
            findViewById<EditText>(R.id.pet_name).setText(profile.name)
            findViewById<EditText>(R.id.pet_species).setText(profile.species)
            findViewById<EditText>(R.id.pet_age).setText(profile.age.toString())
            findViewById<EditText>(R.id.pet_note).setText(profile.note)
        }

        findViewById<Button>(R.id.pet_save_button).setOnClickListener {
            profile.name = findViewById<EditText>(R.id.pet_name).text.toString()
            profile.species = findViewById<EditText>(R.id.pet_species).text.toString()
            profile.age = findViewById<EditText>(R.id.pet_age).text.toString().toIntOrNull() ?: 0
            profile.note = findViewById<EditText>(R.id.pet_note).text.toString()
        }

        // Delete button
        findViewById<Button>(R.id.pet_delete_button).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirm Deletion")
            builder.setMessage("Are you sure you want to delete this profile? This action cannot be undone.")

            builder.setPositiveButton("Yes") { dialog, _ ->
                petList.removeAt(profileIdx)
                dialog.dismiss()
                val intent = Intent(this, PetProfilesActivity::class.java)
                startActivity(intent)
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()
        }

        findViewById<Button>(R.id.pet_go_back_button).setOnClickListener {
            val intent = Intent(this, PetProfilesActivity::class.java)
            startActivity(intent)
        }
    }
}
