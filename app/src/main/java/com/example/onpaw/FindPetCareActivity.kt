package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class FindPetCareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_pet_care)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val chipGroupPets = findViewById<ChipGroup>(R.id.chipsPets)
        if (user.petList.isEmpty()) {
            val chip = layoutInflater.inflate(R.layout.pet_chip, chipGroupPets, false) as Chip
            chip.isCheckable = false
            chip.setText(R.string.create_a_pet_profile)
            chip.setOnClickListener {
                val newPet = Pet()
                user.petList.add(newPet)
                val intent = Intent(this, IndividualPetActivity::class.java)
                intent.putExtra("profileIdx", user.petList.size - 1)
                startActivity(intent)
            }
            chipGroupPets.addView(chip)
        } else {
            for (pet in user.petList) {
                val chip = layoutInflater.inflate(R.layout.pet_chip, chipGroupPets, false) as Chip
                chip.id = View.generateViewId()
                chip.text = pet.name
                chipGroupPets.addView(chip)
            }
            val chip = layoutInflater.inflate(R.layout.pet_chip, chipGroupPets, false) as Chip
            chip.text = "+"
            chip.id = View.generateViewId()
            chip.isCheckable = false
            chip.isClickable = true
            chip.isChecked = false
            chip.setOnClickListener {
                val newPet = Pet()
                user.petList.add(newPet)
                val intent = Intent(this, IndividualPetActivity::class.java)
                intent.putExtra("profileIdx", user.petList.size - 1)
                startActivity(intent)
            }
            chipGroupPets.addView(chip)
        }
        val chipGroupSymptoms = findViewById<ChipGroup>(R.id.chipsSymptoms)
        val chipAddSymptom = findViewById<Chip>(R.id.chipAddSymptom)

        val cardDropoff = findViewById<MaterialCardView>(R.id.cardDropoff)
        val cardCheckup = findViewById<MaterialCardView>(R.id.cardCheckup)
        val cardMeds = findViewById<MaterialCardView>(R.id.cardMeds)
        val cardTransport = findViewById<MaterialCardView>(R.id.cardTransport)

        val etPickup = findViewById<TextInputEditText>(R.id.etPickup)
        val etDestination = findViewById<TextInputEditText>(R.id.etDestination)
        val etNotes = findViewById<TextInputEditText>(R.id.etNotes)

        val btnRequest = findViewById<MaterialButton>(R.id.btnRequest)

        val taskCards: List<Pair<MaterialCardView, String>> = listOf(
            cardDropoff to getString(R.string.task_dropoff),
            cardCheckup to getString(R.string.task_onsite_checkup),
            cardMeds to getString(R.string.task_medication),
            cardTransport to getString(R.string.task_transport)
        )

        val selectedColor = ContextCompat.getColor(this, R.color.pastel_blue)
        val unselectedColor = ContextCompat.getColor(this, android.R.color.white)

        fun applyCardState(card: MaterialCardView, selected: Boolean) {
            card.tag = selected
            if (selected) {
                card.strokeWidth = 0
                card.cardElevation = 6f
                card.setCardBackgroundColor(selectedColor)
            } else {
                card.strokeWidth = 0
                card.cardElevation = 2f
                card.setCardBackgroundColor(unselectedColor)
            }
        }

        taskCards.forEach { (card, _) ->
            applyCardState(card, false)
            card.setOnClickListener {
                val currentlySelected = card.tag as? Boolean ?: false
                applyCardState(card, !currentlySelected)
            }
        }

        chipAddSymptom.setOnClickListener {
            val input = EditText(this).apply {
                hint = getString(R.string.sym_add_hint)
                inputType = InputType.TYPE_CLASS_TEXT
            }

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.sym_add_dialog_title))
                .setView(input)
                .setPositiveButton(getString(R.string.sym_add_dialog_add)) { _, _ ->
                    val text = input.text.toString().trim()
                    if (text.isNotEmpty()) {
                        val newChip = Chip(this).apply {
                            this.text = text
                            isCheckable = true
                            isChecked = true
                            setTextColor(
                                ContextCompat.getColor(
                                    this@FindPetCareActivity,
                                    R.color.text_primary
                                )
                            )
                        }
                        // insert before the "+" chip so it appears after the built-in ones
                        chipGroupSymptoms.addView(newChip, chipGroupSymptoms.childCount - 1)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        btnRequest.setOnClickListener {
            val selectedPetChipId = chipGroupPets.checkedChipId
            val selectedPet = if (selectedPetChipId != -1) {
                chipGroupPets.findViewById<Chip>(selectedPetChipId)?.text?.toString()
                    ?: getString(R.string.select_pet)
            } else {
                getString(R.string.select_pet)
            }

            val selectedSymptoms = chipGroupSymptoms.children
                .filterIsInstance<Chip>()
                .filter { it.isCheckable && it.isChecked }
                .joinToString(separator = ", ") { it.text.toString() }
                .ifEmpty { "None selected" }

            val selectedTasks = taskCards
                .filter { (card, _) -> (card.tag as? Boolean) == true }
                .joinToString(separator = ", ") { it.second }
                .ifEmpty { "None selected" }

            val pickup = etPickup.text?.toString().orEmpty()
            val destination = etDestination.text?.toString().orEmpty()
            val notes = etNotes.text?.toString().orEmpty()

            val message = buildString {
                appendLine("Pet: $selectedPet")
                appendLine("Tasks: $selectedTasks")
                appendLine("Symptoms: $selectedSymptoms")
                appendLine("Pickup: $pickup")
                appendLine("Destination: ${if (destination.isBlank()) "N/A" else destination}")
                appendLine("Notes: ${if (notes.isBlank()) "N/A" else notes}")
            }

            // Get selected pet's species
            val selectedPetSpecies = user.petList.find { it.name == selectedPet }?.species ?: ""

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.find_pet_care_title))
                .setMessage(message)
                .setPositiveButton("Confirm") { _, _ ->
                    val intent = Intent(this, LoadingActivity::class.java)
                    intent.putExtra("petName", selectedPet)
                    intent.putExtra("petSpecies", selectedPetSpecies)
                    intent.putExtra("tasks", selectedTasks)
                    intent.putExtra("symptoms", selectedSymptoms)
                    intent.putExtra("pickup", pickup)
                    intent.putExtra("destination", destination)
                    intent.putExtra("notes", notes)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_find_pet_care, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            R.id.action_profile -> {
                openProfile()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openProfile() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
