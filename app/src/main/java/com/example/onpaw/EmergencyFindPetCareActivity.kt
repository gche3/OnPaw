package com.example.onpaw

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
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

class EmergencyFindPetCareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_emergency_find_pet_care)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val chipGroupAnimalType = findViewById<ChipGroup>(R.id.chipsAnimalType)
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
                                    this@EmergencyFindPetCareActivity,
                                    R.color.text_primary
                                )
                            )
                        }
                        chipGroupSymptoms.addView(newChip, chipGroupSymptoms.childCount - 1)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        btnRequest.setOnClickListener {
            val selectedAnimalChipId = chipGroupAnimalType.checkedChipId
            val selectedAnimalType = if (selectedAnimalChipId != -1) {
                chipGroupAnimalType.findViewById<Chip>(selectedAnimalChipId)?.text?.toString()
                    ?: getString(R.string.select_animal_type)
            } else {
                getString(R.string.select_animal_type)
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
                appendLine("Animal type: $selectedAnimalType")
                appendLine("Tasks: $selectedTasks")
                appendLine("Symptoms: $selectedSymptoms")
                appendLine("Pickup: $pickup")
                appendLine("Destination: ${if (destination.isBlank()) "N/A" else destination}")
                appendLine("Notes: ${if (notes.isBlank()) "N/A" else notes}")
            }

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.request_emergency_care))
                .setMessage(message)
                .setPositiveButton("Confirm") { _, _ ->
                    Toast.makeText(
                        this,
                        "Emergency care request sent (mock)!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_emergency, menu)
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
        AlertDialog.Builder(this)
            .setTitle("Profile")
            .setMessage("Profile screen would open here.")
            .setPositiveButton("OK", null)
            .show()
    }
}
