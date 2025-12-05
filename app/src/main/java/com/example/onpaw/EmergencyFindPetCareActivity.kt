package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText

class EmergencyFindPetCareActivity : AppCompatActivity() {

    private val totalSteps = 4
    private var currentStep = 0

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_find_pet_care)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tvStepTitle = findViewById<TextView>(R.id.tvStepTitle)
        val stepProgress = findViewById<LinearProgressIndicator>(R.id.stepProgress)

        val stepAnimal = findViewById<View>(R.id.stepAnimal)
        val stepTasks = findViewById<View>(R.id.stepTasks)
        val stepSymptoms = findViewById<View>(R.id.stepSymptoms)
        val stepLocation = findViewById<View>(R.id.stepLocation)

        val btnBack = findViewById<MaterialButton>(R.id.btnBack)
        val btnRequest = findViewById<MaterialButton>(R.id.btnRequest)

        val animalContainer = findViewById<LinearLayout>(R.id.animalOptionList)

        val cardAnimalDog = findViewById<MaterialCardView>(R.id.cardAnimalDog)
        val cardAnimalCat = findViewById<MaterialCardView>(R.id.cardAnimalCat)
        val cardAnimalBunny = findViewById<MaterialCardView>(R.id.cardAnimalBunny)
        val cardAnimalHamster = findViewById<MaterialCardView>(R.id.cardAnimalHamster)
        val cardAnimalAdd = findViewById<MaterialCardView>(R.id.cardAnimalAdd)

        val tvAnimalDog = findViewById<TextView>(R.id.tvAnimalDog)
        val tvAnimalCat = findViewById<TextView>(R.id.tvAnimalCat)
        val tvAnimalBunny = findViewById<TextView>(R.id.tvAnimalBunny)
        val tvAnimalHamster = findViewById<TextView>(R.id.tvAnimalHamster)
        val tvAnimalAdd = findViewById<TextView>(R.id.tvAnimalAdd)

        val animalCards = mutableListOf<MaterialCardView>()
        val animalLabels = mutableMapOf<MaterialCardView, String>()

        fun registerAnimalCard(card: MaterialCardView, label: String) {
            animalCards.add(card)
            animalLabels[card] = label
        }

        registerAnimalCard(cardAnimalDog, tvAnimalDog.text.toString())
        registerAnimalCard(cardAnimalCat, tvAnimalCat.text.toString())
        registerAnimalCard(cardAnimalBunny, tvAnimalBunny.text.toString())
        registerAnimalCard(cardAnimalHamster, tvAnimalHamster.text.toString())

        var selectedAnimal: String = tvAnimalDog.text.toString()

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

        fun selectAnimalCard(card: MaterialCardView) {
            animalCards.forEach { applyCardState(it, it == card) }
            selectedAnimal = animalLabels[card] ?: ""
        }

        animalCards.forEach { applyCardState(it, false) }
        applyCardState(cardAnimalAdd, false)
        selectAnimalCard(cardAnimalDog)

        cardAnimalDog.setOnClickListener { selectAnimalCard(cardAnimalDog) }
        cardAnimalCat.setOnClickListener { selectAnimalCard(cardAnimalCat) }
        cardAnimalBunny.setOnClickListener { selectAnimalCard(cardAnimalBunny) }
        cardAnimalHamster.setOnClickListener { selectAnimalCard(cardAnimalHamster) }

        fun createAnimalCard(label: String): MaterialCardView {
            val template = cardAnimalDog
            val lpTemplate = template.layoutParams as LinearLayout.LayoutParams

            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    lpTemplate.height
                ).apply {
                    bottomMargin = 12.dpToPx()
                }
                radius = template.radius
                cardElevation = template.cardElevation
                useCompatPadding = template.useCompatPadding
            }

            val inner = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16.dpToPx(), 0, 16.dpToPx(), 0)
            }

            val tv = TextView(this).apply {
                text = label
                textSize = 16f
                setTextColor(
                    ContextCompat.getColor(
                        this@EmergencyFindPetCareActivity,
                        R.color.text_primary
                    )
                )
            }

            inner.addView(tv)
            card.addView(inner)

            return card
        }

        cardAnimalAdd.setOnClickListener {
            val input = EditText(this).apply {
                hint = "Enter animal type"
            }

            AlertDialog.Builder(this)
                .setTitle("Add animal type")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val text = input.text.toString().trim()
                    if (text.isNotEmpty()) {
                        val newCard = createAnimalCard(text)

                        registerAnimalCard(newCard, text)

                        newCard.setOnClickListener {
                            selectAnimalCard(newCard)
                        }

                        val addIndex = animalContainer.indexOfChild(cardAnimalAdd)
                        animalContainer.addView(newCard, addIndex)

                        selectAnimalCard(newCard)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        val gridTasks = findViewById<GridLayout>(R.id.gridTasks)

        val cardDropoff = findViewById<MaterialCardView>(R.id.cardDropoff)
        val cardCheckup = findViewById<MaterialCardView>(R.id.cardCheckup)
        val cardMeds = findViewById<MaterialCardView>(R.id.cardMeds)
        val cardTransport = findViewById<MaterialCardView>(R.id.cardTransport)

        val tvTaskDropoff = findViewById<TextView>(R.id.tvTaskDropoff)
        val tvTaskCheckup = findViewById<TextView>(R.id.tvTaskCheckup)
        val tvTaskMeds = findViewById<TextView>(R.id.tvTaskMeds)
        val tvTaskTransport = findViewById<TextView>(R.id.tvTaskTransport)

        val taskCards = mutableListOf<MaterialCardView>()
        val taskLabels = mutableMapOf<MaterialCardView, String>()

        fun registerTaskCard(card: MaterialCardView, label: String) {
            taskCards.add(card)
            taskLabels[card] = label
        }

        registerTaskCard(cardDropoff, tvTaskDropoff.text.toString())
        registerTaskCard(cardCheckup, tvTaskCheckup.text.toString())
        registerTaskCard(cardMeds, tvTaskMeds.text.toString())
        registerTaskCard(cardTransport, tvTaskTransport.text.toString())

        taskCards.forEach { applyCardState(it, false) }

        taskCards.forEach { card ->
            card.setOnClickListener {
                val currentlySelected = card.tag as? Boolean ?: false
                applyCardState(card, !currentlySelected)
            }
        }

        val symptomContainer = findViewById<LinearLayout>(R.id.symptomOptionList)

        val cardSymLethargy = findViewById<MaterialCardView>(R.id.cardSymLethargy)
        val cardSymVomit = findViewById<MaterialCardView>(R.id.cardSymVomit)
        val cardSymBreathing = findViewById<MaterialCardView>(R.id.cardSymBreathing)
        val cardSymInjury = findViewById<MaterialCardView>(R.id.cardSymInjury)
        val cardSymSeizure = findViewById<MaterialCardView>(R.id.cardSymSeizure)
        val cardSymAdd = findViewById<MaterialCardView>(R.id.cardSymAdd)

        val tvSymLethargy = findViewById<TextView>(R.id.tvSymLethargy)
        val tvSymVomit = findViewById<TextView>(R.id.tvSymVomit)
        val tvSymBreathing = findViewById<TextView>(R.id.tvSymBreathing)
        val tvSymInjury = findViewById<TextView>(R.id.tvSymInjury)
        val tvSymSeizure = findViewById<TextView>(R.id.tvSymSeizure)
        val tvSymAdd = findViewById<TextView>(R.id.tvSymAdd)

        val symptomCards = mutableListOf<MaterialCardView>()
        val symptomLabels = mutableMapOf<MaterialCardView, String>()

        fun registerSymptomCard(card: MaterialCardView, label: String) {
            symptomCards.add(card)
            symptomLabels[card] = label
        }

        registerSymptomCard(cardSymLethargy, tvSymLethargy.text.toString())
        registerSymptomCard(cardSymVomit, tvSymVomit.text.toString())
        registerSymptomCard(cardSymBreathing, tvSymBreathing.text.toString())
        registerSymptomCard(cardSymInjury, tvSymInjury.text.toString())
        registerSymptomCard(cardSymSeizure, tvSymSeizure.text.toString())

        symptomCards.forEach { applyCardState(it, false) }
        applyCardState(cardSymAdd, false)

        symptomCards.forEach { card ->
            card.setOnClickListener {
                val currentlySelected = card.tag as? Boolean ?: false
                applyCardState(card, !currentlySelected)
            }
        }

        fun createSymptomCard(label: String): MaterialCardView {
            val template = cardSymLethargy
            val lpTemplate = template.layoutParams as LinearLayout.LayoutParams

            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    lpTemplate.height
                ).apply {
                    bottomMargin = 12.dpToPx()
                }
                radius = template.radius
                cardElevation = template.cardElevation
                useCompatPadding = template.useCompatPadding
            }

            val tv = TextView(this).apply {
                text = label
                textSize = 16f
                setTextColor(
                    ContextCompat.getColor(
                        this@EmergencyFindPetCareActivity,
                        R.color.text_primary
                    )
                )
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16.dpToPx(), 0, 16.dpToPx(), 0)
            }

            card.addView(tv)
            return card
        }

        cardSymAdd.setOnClickListener {
            val input = EditText(this).apply {
                hint = "Describe symptom"
            }

            AlertDialog.Builder(this)
                .setTitle("Add symptom")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val text = input.text.toString().trim()
                    if (text.isNotEmpty()) {
                        val newCard = createSymptomCard(text)
                        registerSymptomCard(newCard, text)
                        applyCardState(newCard, true)

                        newCard.setOnClickListener {
                            val currentlySelected = newCard.tag as? Boolean ?: false
                            applyCardState(newCard, !currentlySelected)
                        }

                        val addIndex = symptomContainer.indexOfChild(cardSymAdd)
                        symptomContainer.addView(newCard, addIndex)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        val etPickup = findViewById<TextInputEditText>(R.id.etPickup)
        val etDestination = findViewById<TextInputEditText>(R.id.etDestination)
        val etNotes = findViewById<TextInputEditText>(R.id.etNotes)

        fun updateStepUI() {
            stepAnimal.visibility = if (currentStep == 0) View.VISIBLE else View.GONE
            stepTasks.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
            stepSymptoms.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
            stepLocation.visibility = if (currentStep == 3) View.VISIBLE else View.GONE

            tvStepTitle.text = "Step ${currentStep + 1} of $totalSteps"

            val progress = ((currentStep + 1) * 100) / totalSteps
            stepProgress.setProgress(progress, true)

            btnBack.isEnabled = currentStep > 0
            btnRequest.text = if (currentStep == totalSteps - 1) "Find Care" else "Next"
        }

        btnBack.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                updateStepUI()
            }
        }

        btnRequest.setOnClickListener {
            if (currentStep < totalSteps - 1) {
                currentStep++
                updateStepUI()
            } else {
                val selectedTasksSummary = taskCards
                    .filter { (it.tag as? Boolean) == true }
                    .mapNotNull { taskLabels[it] }
                    .joinToString(", ")
                    .ifEmpty { "None selected" }

                val selectedSymptomsSummary = symptomCards
                    .filter { (it.tag as? Boolean) == true }
                    .mapNotNull { symptomLabels[it] }
                    .joinToString(", ")
                    .ifEmpty { "None selected" }

                val pickup = etPickup.text?.toString().orEmpty()
                val destination = etDestination.text?.toString().orEmpty()
                val notes = etNotes.text?.toString().orEmpty()

                val animalSummary = selectedAnimal.ifBlank { "Unknown" }

                val message = buildString {
                    appendLine("Animal: $animalSummary")
                    appendLine("Tasks: $selectedTasksSummary")
                    appendLine("Symptoms: $selectedSymptomsSummary")
                    appendLine("Pickup: $pickup")
                    appendLine("Destination: ${if (destination.isBlank()) "N/A" else destination}")
                    appendLine("Notes: ${if (notes.isBlank()) "N/A" else notes}")
                }

                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.find_pet_care_title))
                    .setMessage(message)
                    .setPositiveButton("Confirm") { _, _ ->
                        startActivity(Intent(this, LoadingActivity::class.java))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        updateStepUI()
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
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
