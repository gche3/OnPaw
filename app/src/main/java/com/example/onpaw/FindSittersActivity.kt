package com.example.onpaw

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.*

class FindSittersActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var currentSitter: PetSitter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        
        setContentView(R.layout.activity_find_sitters)

        // Initialize map
        setupMap()
        
        // Update UI with sitter info
        updateSitterInfo()

        // Back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // Cancel button
        findViewById<ImageView>(R.id.btn_cancel).setOnClickListener {
            showCancelConfirmationDialog()
        }

        // Call button
        findViewById<ImageView>(R.id.btn_call).setOnClickListener {
            val phoneNumber = "1234567890" // Replace with actual sitter phone
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }

        // Send message button
        findViewById<ImageView>(R.id.btn_send_message).setOnClickListener {
            val messageInput = findViewById<EditText>(R.id.message_input)
            val message = messageInput.text.toString().trim()
            
            if (message.isNotEmpty()) {
                Toast.makeText(this, "Message sent: $message", Toast.LENGTH_SHORT).show()
                messageInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        // Quick action buttons (placeholders)
        findViewById<Button>(R.id.btn_pet_safe).setOnClickListener {
            Toast.makeText(this, "Is pet safe?", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_back_door).setOnClickListener {
            Toast.makeText(this, "Use back door", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_photo).setOnClickListener {
            Toast.makeText(this, "Photo of pet requested", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMap() {
        mapView = findViewById(R.id.map_view)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        
        // Use user's actual location from data
        val mapController = mapView.controller
        mapController.setZoom(14.5)
        val userLocation = GeoPoint(user.latitude, user.longitude)
        mapController.setCenter(userLocation)

        // Add user location marker
        val userMarker = Marker(mapView)
        userMarker.position = userLocation
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        userMarker.title = "Your Location\n${user.address}"
        mapView.overlays.add(userMarker)

        // Add only the closest pet sitter marker
        val closestSitters = getClosestPetSitters(user.latitude, user.longitude, 1)
        if (closestSitters.isNotEmpty()) {
            currentSitter = closestSitters[0]
            val sitter = currentSitter!!
            val sitterMarker = Marker(mapView)
            sitterMarker.position = GeoPoint(sitter.latitude, sitter.longitude)
            sitterMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            sitterMarker.title = "${sitter.name}\n⭐ ${sitter.rating} - $${sitter.hourlyRate}/hr"
            sitterMarker.snippet = sitter.bio
            mapView.overlays.add(sitterMarker)
            
            // Draw route line between user and sitter
            val routeLine = Polyline()
            routeLine.addPoint(userLocation)
            routeLine.addPoint(GeoPoint(sitter.latitude, sitter.longitude))
            routeLine.outlinePaint.color = Color.parseColor("#97B3AE") // pastel_blue
            routeLine.outlinePaint.strokeWidth = 8f
            mapView.overlays.add(0, routeLine) // Add at index 0 so it's behind markers
        }
    }
    
    private fun updateSitterInfo() {
        if (currentSitter != null) {
            val sitter = currentSitter!!
            
            // Calculate distance and ETA
            val distance = calculateDistance(user.latitude, user.longitude, sitter.latitude, sitter.longitude)
            val eta = calculateETA(distance)
            
            // Update sitter name
            findViewById<TextView>(R.id.sitter_name).text = sitter.name
            
            // Update ETA text
            findViewById<TextView>(R.id.eta_text).text = eta
            
            // Update destination text to show sitter coming to user
            findViewById<TextView>(R.id.destination_text).text = "Destination: ${user.address}"
        }
    }
    
    // Calculate distance between two coordinates in miles using Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 3958.8 // Earth radius in miles
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) + 
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * 
                sin(dLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    // Calculate ETA assuming average speed of 30 mph in city
    private fun calculateETA(distanceInMiles: Double): String {
        val averageSpeedMph = 30.0
        val timeInHours = distanceInMiles / averageSpeedMph
        val timeInMinutes = (timeInHours * 60).roundToInt()
        
        return if (timeInMinutes < 1) {
            "Arriving now"
        } else if (timeInMinutes == 1) {
            "1 min away"
        } else {
            "$timeInMinutes min away"
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    private fun showCancelConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Cancel Booking")
        builder.setMessage("Are you sure you want to cancel?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            finish() // Return to home screen
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}

