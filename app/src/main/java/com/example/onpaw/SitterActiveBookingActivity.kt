package com.example.onpaw

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class SitterActiveBookingActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var bookingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        
        setContentView(R.layout.activity_sitter_active_booking)

        bookingId = intent.getStringExtra("bookingId")

        // Initialize views
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnCall = findViewById<ImageView>(R.id.btn_call)
        val tvOwnerName = findViewById<TextView>(R.id.tvOwnerName)
        val tvPetInfo = findViewById<TextView>(R.id.tvPetInfo)
        val tvDestination = findViewById<TextView>(R.id.tvDestination)
        val tvTasks = findViewById<TextView>(R.id.tvTasks)
        val tvNotes = findViewById<TextView>(R.id.tvNotes)
        val btnChat = findViewById<MaterialButton>(R.id.btnChat)
        val btnComplete = findViewById<MaterialButton>(R.id.btnComplete)
        val btnCancel = findViewById<MaterialButton>(R.id.btnCancel)

        // Setup map
        setupMap()

        // Load booking data
        val booking = currentBooking
        if (booking != null) {
            tvOwnerName.text = booking.ownerName
            tvPetInfo.text = "Pet: ${booking.petName} (${booking.petSpecies})"
            tvDestination.text = "Pickup: ${booking.pickupAddress.ifEmpty { "Not specified" }}"
            tvTasks.text = "Tasks: ${booking.tasks.ifEmpty { "None specified" }}"
            tvNotes.text = "Notes: ${booking.notes.ifEmpty { "None" }}"
            
            // Update map with owner location
            updateMapWithBooking(booking)
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnCall.setOnClickListener {
            // In fake mode, we don't have owner phone, show message
            if (AppConfig.USE_FIREBASE) {
                // Would get phone from Firebase
                Toast.makeText(this, "Calling owner...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Owner phone: (simulated)", Toast.LENGTH_SHORT).show()
            }
        }

        btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("sitterName", booking?.ownerName ?: "Owner")
            intent.putExtra("isSitterView", true)
            startActivity(intent)
        }

        btnComplete.setOnClickListener {
            showCompleteConfirmation()
        }

        btnCancel.setOnClickListener {
            showCancelConfirmation()
        }
    }

    private fun setupMap() {
        mapView = findViewById(R.id.map_view)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        
        val mapController = mapView.controller
        mapController.setZoom(14.5)
        
        // Center on sitter location
        val sitter = SitterLoginActivity.currentSitter
        if (sitter != null) {
            val sitterLocation = GeoPoint(sitter.latitude, sitter.longitude)
            mapController.setCenter(sitterLocation)
            
            // Add sitter marker
            val sitterMarker = Marker(mapView)
            sitterMarker.position = sitterLocation
            sitterMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            sitterMarker.title = "Your Location"
            mapView.overlays.add(sitterMarker)
        }
    }

    private fun updateMapWithBooking(booking: BookingRequest) {
        if (booking.ownerLatitude != 0.0 && booking.ownerLongitude != 0.0) {
            val ownerLocation = GeoPoint(booking.ownerLatitude, booking.ownerLongitude)
            
            // Add owner marker
            val ownerMarker = Marker(mapView)
            ownerMarker.position = ownerLocation
            ownerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            ownerMarker.title = "${booking.ownerName}\n${booking.pickupAddress}"
            mapView.overlays.add(ownerMarker)
            
            // Draw route line
            val sitter = SitterLoginActivity.currentSitter
            if (sitter != null) {
                val sitterLocation = GeoPoint(sitter.latitude, sitter.longitude)
                val routeLine = Polyline()
                routeLine.addPoint(sitterLocation)
                routeLine.addPoint(ownerLocation)
                routeLine.outlinePaint.color = Color.parseColor("#97B3AE")
                routeLine.outlinePaint.strokeWidth = 8f
                mapView.overlays.add(0, routeLine)
                
                // Center map between both points
                val centerLat = (sitter.latitude + booking.ownerLatitude) / 2
                val centerLon = (sitter.longitude + booking.ownerLongitude) / 2
                mapView.controller.setCenter(GeoPoint(centerLat, centerLon))
            }
        }
    }

    private fun showCompleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Complete Booking")
            .setMessage("Mark this booking as completed?")
            .setPositiveButton("Yes") { _, _ ->
                completeBooking()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun completeBooking() {
        if (AppConfig.USE_FIREBASE && bookingId != null) {
            FirebaseRepository.updateBookingStatus(
                bookingId = bookingId!!,
                status = BookingStatus.COMPLETED,
                onSuccess = {
                    Toast.makeText(this, "Booking completed! Great job!", Toast.LENGTH_SHORT).show()
                    currentBooking = null
                    finish()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Fake data mode
            currentBooking?.status = BookingStatus.COMPLETED
            currentBooking = null
            Toast.makeText(this, "Booking completed! Great job!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes") { _, _ ->
                cancelBooking()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelBooking() {
        if (AppConfig.USE_FIREBASE && bookingId != null) {
            FirebaseRepository.updateBookingStatus(
                bookingId = bookingId!!,
                status = BookingStatus.CANCELLED,
                onSuccess = {
                    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show()
                    currentBooking = null
                    finish()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Fake data mode
            currentBooking = null
            Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show()
            finish()
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
}

