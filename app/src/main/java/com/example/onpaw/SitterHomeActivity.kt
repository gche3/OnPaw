package com.example.onpaw

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.ListenerRegistration

class SitterHomeActivity : AppCompatActivity() {

    private lateinit var switchAvailability: SwitchMaterial
    private lateinit var tvAvailabilityStatus: TextView
    private lateinit var recyclerRequests: RecyclerView
    private lateinit var emptyState: View
    private lateinit var requestAdapter: BookingRequestAdapter
    
    private var bookingListener: ListenerRegistration? = null
    private val pendingRequests = mutableListOf<BookingRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sitter_home)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            showLogoutConfirmation()
        }

        // Initialize views
        switchAvailability = findViewById(R.id.switchAvailability)
        tvAvailabilityStatus = findViewById(R.id.tvAvailabilityStatus)
        recyclerRequests = findViewById(R.id.recyclerRequests)
        emptyState = findViewById(R.id.emptyState)

        // Setup RecyclerView
        requestAdapter = BookingRequestAdapter(pendingRequests) { request, accepted ->
            handleRequestResponse(request, accepted)
        }
        recyclerRequests.layoutManager = LinearLayoutManager(this)
        recyclerRequests.adapter = requestAdapter

        // Availability toggle
        switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            updateAvailability(isChecked)
        }

        // Settings button
        findViewById<MaterialButton>(R.id.btnSettings).setOnClickListener {
            // For now, just show a toast
            Toast.makeText(this, "Sitter settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Load initial data
        loadPendingRequests()
    }

    private fun updateAvailability(isAvailable: Boolean) {
        tvAvailabilityStatus.text = if (isAvailable) "You are available" else "You are offline"
        
        if (AppConfig.USE_FIREBASE) {
            FirebaseRepository.updateSitterAvailability(
                isAvailable = isAvailable,
                onSuccess = {
                    Toast.makeText(
                        this,
                        if (isAvailable) "You're now available for requests" else "You're now offline",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun loadPendingRequests() {
        if (AppConfig.USE_FIREBASE) {
            bookingListener = FirebaseRepository.getPendingBookingsForSitter(
                onSuccess = { requests ->
                    pendingRequests.clear()
                    pendingRequests.addAll(requests)
                    updateUI()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Fake data mode - show current booking if exists and assigned to this sitter
            pendingRequests.clear()
            currentBooking?.let { booking ->
                val sitter = SitterLoginActivity.currentSitter
                if (sitter != null && booking.sitterName == sitter.name && booking.status == BookingStatus.PENDING) {
                    pendingRequests.add(booking)
                }
            }
            updateUI()
        }
    }

    private fun updateUI() {
        if (pendingRequests.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerRequests.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerRequests.visibility = View.VISIBLE
            requestAdapter.notifyDataSetChanged()
        }
    }

    private fun handleRequestResponse(request: BookingRequest, accepted: Boolean) {
        if (accepted) {
            if (AppConfig.USE_FIREBASE) {
                FirebaseRepository.updateBookingStatus(
                    bookingId = request.id,
                    status = BookingStatus.ACCEPTED,
                    onSuccess = {
                        Toast.makeText(this, "Booking accepted!", Toast.LENGTH_SHORT).show()
                        // Navigate to active booking screen
                        val intent = Intent(this, SitterActiveBookingActivity::class.java)
                        intent.putExtra("bookingId", request.id)
                        startActivity(intent)
                    },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Fake data mode
                currentBooking?.status = BookingStatus.ACCEPTED
                Toast.makeText(this, "Booking accepted!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SitterActiveBookingActivity::class.java)
                startActivity(intent)
            }
        } else {
            // Decline
            if (AppConfig.USE_FIREBASE) {
                FirebaseRepository.updateBookingStatus(
                    bookingId = request.id,
                    status = BookingStatus.CANCELLED,
                    onSuccess = {
                        Toast.makeText(this, "Booking declined", Toast.LENGTH_SHORT).show()
                        pendingRequests.remove(request)
                        updateUI()
                    },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Fake data mode
                currentBooking = null
                pendingRequests.remove(request)
                updateUI()
                Toast.makeText(this, "Booking declined", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                if (AppConfig.USE_FIREBASE) {
                    FirebaseRepository.signOut()
                }
                SitterLoginActivity.currentSitter = null
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadPendingRequests()
    }

    override fun onDestroy() {
        super.onDestroy()
        bookingListener?.remove()
    }

    // Adapter for booking requests
    class BookingRequestAdapter(
        private val requests: List<BookingRequest>,
        private val onResponse: (BookingRequest, Boolean) -> Unit
    ) : RecyclerView.Adapter<BookingRequestAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvOwnerName: TextView = view.findViewById(R.id.tvOwnerName)
            val tvPetInfo: TextView = view.findViewById(R.id.tvPetInfo)
            val tvDistance: TextView = view.findViewById(R.id.tvDistance)
            val tvTasks: TextView = view.findViewById(R.id.tvTasks)
            val tvSymptoms: TextView = view.findViewById(R.id.tvSymptoms)
            val tvPickupAddress: TextView = view.findViewById(R.id.tvPickupAddress)
            val btnAccept: MaterialButton = view.findViewById(R.id.btnAccept)
            val btnDecline: MaterialButton = view.findViewById(R.id.btnDecline)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_booking_request, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val request = requests[position]
            
            holder.tvOwnerName.text = request.ownerName
            holder.tvPetInfo.text = "Pet: ${request.petName} (${request.petSpecies})"
            holder.tvTasks.text = "Tasks: ${request.tasks.ifEmpty { "None specified" }}"
            holder.tvSymptoms.text = "Symptoms: ${request.symptoms.ifEmpty { "None" }}"
            holder.tvPickupAddress.text = "Pickup: ${request.pickupAddress.ifEmpty { "Not specified" }}"
            
            // Calculate distance from sitter
            val sitter = SitterLoginActivity.currentSitter
            if (sitter != null && request.ownerLatitude != 0.0) {
                val distance = calculateDistanceKm(
                    sitter.latitude, sitter.longitude,
                    request.ownerLatitude, request.ownerLongitude
                )
                holder.tvDistance.text = String.format("%.1f km", distance)
            } else {
                holder.tvDistance.text = "N/A"
            }

            holder.btnAccept.setOnClickListener { onResponse(request, true) }
            holder.btnDecline.setOnClickListener { onResponse(request, false) }
        }

        override fun getItemCount() = requests.size
    }
}

