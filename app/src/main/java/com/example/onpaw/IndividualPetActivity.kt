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
        if (profileIdx !in user.petList.indices) {
            finish()
            return
        }

        val profile = user.petList[profileIdx]

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
            val nameField = findViewById<EditText>(R.id.pet_name)
            val speciesField = findViewById<EditText>(R.id.pet_species)
            val ageField = findViewById<EditText>(R.id.pet_age)
            val noteField = findViewById<EditText>(R.id.pet_note)

            val name = nameField.text.toString().trim()
            val species = speciesField.text.toString().trim()
            val age = ageField.text.toString().toIntOrNull() ?: 0
            val note = noteField.text.toString().trim()

            when {
                name.isEmpty() -> {
                    nameField.error = "Pet name cannot be empty"
                }
                species.isEmpty() -> {
                    speciesField.error = "Species cannot be empty"
                }
                else -> {
                    profile.name = name
                    profile.species = species
                    profile.age = age
                    profile.note = note

                    AlertDialog.Builder(this)
                        .setTitle("Saved")
                        .setMessage("Pet profile updated successfully.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            val idx = userList.indexOfFirst { it.email == user.email }
            if (idx != -1) {
                userList[idx].petList = user.petList
            }
        }

        findViewById<Button>(R.id.pet_delete_button).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirm Deletion")
            builder.setMessage("Are you sure you want to delete this profile? This action cannot be undone.")

            builder.setPositiveButton("Yes") { dialog, _ ->
                user.petList.removeAt(profileIdx)
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
            if (profile.name == "") {
                user.petList.removeAt(profileIdx)
                finish()
            } else {
                val intent = Intent(this, PetProfilesActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
