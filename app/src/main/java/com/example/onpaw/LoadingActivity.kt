package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Get booking details from intent (passed from FindPetCareActivity)
        val petName = intent.getStringExtra("petName") ?: "Your pet"
        val petSpecies = intent.getStringExtra("petSpecies") ?: ""
        val tasks = intent.getStringExtra("tasks") ?: ""
        val symptoms = intent.getStringExtra("symptoms") ?: ""
        val pickup = intent.getStringExtra("pickup") ?: ""
        val destination = intent.getStringExtra("destination") ?: ""
        val notes = intent.getStringExtra("notes") ?: ""

        // Find closest available sitter
        findAndMatchSitter(petName, petSpecies, tasks, symptoms, pickup, destination, notes)
    }

    private fun findAndMatchSitter(
        petName: String,
        petSpecies: String,
        tasks: String,
        symptoms: String,
        pickup: String,
        destination: String,
        notes: String
    ) {
        if (AppConfig.USE_FIREBASE) {
            // Firebase mode - find available sitters from database
            FirebaseRepository.getAvailableSitters(
                onSuccess = { sitters ->
                    if (sitters.isEmpty()) {
                        Toast.makeText(this, "No sitters available right now", Toast.LENGTH_LONG).show()
                        finish()
                        return@getAvailableSitters
                    }

                    // Find closest sitter using Haversine formula
                    val closestSitter = sitters.minByOrNull { sitter ->
                        calculateDistanceKm(user.latitude, user.longitude, sitter.latitude, sitter.longitude)
                    }

                    if (closestSitter != null) {
                        createBookingAndNavigate(closestSitter, petName, petSpecies, tasks, symptoms, pickup, destination, notes)
                    } else {
                        Toast.makeText(this, "No sitters available", Toast.LENGTH_LONG).show()
                        finish()
                    }
                },
                onError = { error ->
                    Toast.makeText(this, "Error finding sitters: $error", Toast.LENGTH_LONG).show()
                    finish()
                }
            )
        } else {
            // Fake data mode - use local sitter list
            Handler(Looper.getMainLooper()).postDelayed({
                val closestSitter = findClosestAvailableSitter(user.latitude, user.longitude)
                
                if (closestSitter != null) {
                    // Create booking request
                    val booking = BookingRequest(
                        id = "fake_${System.currentTimeMillis()}",
                        ownerId = "fake_owner",
                        ownerName = user.name,
                        sitterId = closestSitter.id.toString(),
                        sitterName = closestSitter.name,
                        petName = petName,
                        petSpecies = petSpecies,
                        tasks = tasks,
                        symptoms = symptoms,
                        pickupAddress = pickup.ifEmpty { user.address },
                        destinationAddress = destination,
                        notes = notes,
                        status = BookingStatus.PENDING,
                        ownerLatitude = user.latitude,
                        ownerLongitude = user.longitude
                    )
                    
                    // Store booking and matched sitter globally
                    currentBooking = booking
                    matchedSitter = closestSitter
                    
                    // Navigate to FindSittersActivity
                    val intent = Intent(this, FindSittersActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "No sitters available", Toast.LENGTH_LONG).show()
                    finish()
                }
            }, 2000) // 2 second delay to show loading
        }
    }

    private fun createBookingAndNavigate(
        sitter: PetSitter,
        petName: String,
        petSpecies: String,
        tasks: String,
        symptoms: String,
        pickup: String,
        destination: String,
        notes: String
    ) {
        val booking = BookingRequest(
            ownerName = user.name,
            sitterId = sitter.id.toString(),
            sitterName = sitter.name,
            petName = petName,
            petSpecies = petSpecies,
            tasks = tasks,
            symptoms = symptoms,
            pickupAddress = pickup.ifEmpty { user.address },
            destinationAddress = destination,
            notes = notes,
            ownerLatitude = user.latitude,
            ownerLongitude = user.longitude
        )

        FirebaseRepository.createBooking(
            request = booking,
            onSuccess = { bookingId ->
                // Store locally for UI
                currentBooking = booking.copy(id = bookingId)
                matchedSitter = sitter
                
                // Navigate to FindSittersActivity
                val intent = Intent(this, FindSittersActivity::class.java)
                intent.putExtra("bookingId", bookingId)
                startActivity(intent)
                finish()
            },
            onError = { error ->
                Toast.makeText(this, "Error creating booking: $error", Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }
}

