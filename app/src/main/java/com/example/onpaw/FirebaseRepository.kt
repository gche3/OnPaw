package com.example.onpaw

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Repository for all Firebase operations.
 * Only used when AppConfig.USE_FIREBASE is true.
 */
object FirebaseRepository {
    
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
    // Collection names
    private const val USERS_COLLECTION = "users"
    private const val SITTERS_COLLECTION = "sitters"
    private const val BOOKINGS_COLLECTION = "bookings"
    private const val MESSAGES_COLLECTION = "messages"
    
    // Current user role
    var currentUserRole: UserRole = UserRole.OWNER
        private set
    
    enum class UserRole {
        OWNER, SITTER
    }
    
    // ==================== AUTH ====================
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    val isLoggedIn: Boolean
        get() = auth.currentUser != null
    
    fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: UserRole,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                
                // Create user document in Firestore
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to role.name,
                    "latitude" to 40.1020,
                    "longitude" to -88.2282,
                    "address" to ""
                )
                
                db.collection(USERS_COLLECTION).document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        currentUserRole = role
                        
                        // If sitter, also create sitter profile
                        if (role == UserRole.SITTER) {
                            createSitterProfile(userId, name, email, phone, onSuccess, onError)
                        } else {
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to create user profile")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Sign up failed")
            }
    }
    
    private fun createSitterProfile(
        userId: String,
        name: String,
        email: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val sitterData = hashMapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "bio" to "New pet sitter ready to help!",
            "hourlyRate" to 20.0,
            "rating" to 5.0,
            "latitude" to 40.1020,
            "longitude" to -88.2282,
            "address" to "",
            "isAvailable" to true,
            "petTypes" to listOf("Dog", "Cat"),
            "yearsExperience" to 1
        )
        
        db.collection(SITTERS_COLLECTION).document(userId)
            .set(sitterData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to create sitter profile") }
    }
    
    fun signIn(
        email: String,
        password: String,
        role: UserRole,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                
                // Verify user role
                db.collection(USERS_COLLECTION).document(userId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val storedRole = doc.getString("role")
                        if (storedRole == role.name) {
                            currentUserRole = role
                            onSuccess()
                        } else {
                            auth.signOut()
                            onError("This account is registered as a ${storedRole?.lowercase()}. Please use the correct login.")
                        }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to verify user")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Sign in failed")
            }
    }
    
    fun signOut() {
        auth.signOut()
        currentUserRole = UserRole.OWNER
    }
    
    // ==================== USER DATA ====================
    
    fun getCurrentUserData(onSuccess: (Map<String, Any>) -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("Not logged in")
        
        db.collection(USERS_COLLECTION).document(userId)
            .get()
            .addOnSuccessListener { doc ->
                doc.data?.let { onSuccess(it) } ?: onError("User data not found")
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to get user data")
            }
    }
    
    fun updateUserData(data: Map<String, Any>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("Not logged in")
        
        db.collection(USERS_COLLECTION).document(userId)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update user data") }
    }
    
    // ==================== SITTERS ====================
    
    fun getAvailableSitters(onSuccess: (List<PetSitter>) -> Unit, onError: (String) -> Unit) {
        db.collection(SITTERS_COLLECTION)
            .whereEqualTo("isAvailable", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val sitters = snapshot.documents.mapNotNull { doc ->
                    try {
                        PetSitter(
                            id = doc.id.hashCode(),
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            address = doc.getString("address") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            rating = doc.getDouble("rating") ?: 5.0,
                            hourlyRate = doc.getDouble("hourlyRate") ?: 20.0,
                            bio = doc.getString("bio") ?: "",
                            availability = "Available",
                            petTypes = (doc.get("petTypes") as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                            yearsExperience = doc.getLong("yearsExperience")?.toInt() ?: 1
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onSuccess(sitters)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to get sitters")
            }
    }
    
    fun updateSitterAvailability(isAvailable: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onError("Not logged in")
        
        db.collection(SITTERS_COLLECTION).document(userId)
            .update("isAvailable", isAvailable)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update availability") }
    }
    
    fun updateSitterLocation(latitude: Double, longitude: Double) {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection(SITTERS_COLLECTION).document(userId)
            .update(
                mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
            )
    }
    
    // ==================== BOOKINGS ====================
    
    fun createBooking(
        request: BookingRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onError("Not logged in")
        
        val bookingData = hashMapOf(
            "ownerId" to userId,
            "ownerName" to request.ownerName,
            "sitterId" to request.sitterId,
            "sitterName" to request.sitterName,
            "petName" to request.petName,
            "petSpecies" to request.petSpecies,
            "tasks" to request.tasks,
            "symptoms" to request.symptoms,
            "pickupAddress" to request.pickupAddress,
            "destinationAddress" to request.destinationAddress,
            "notes" to request.notes,
            "status" to BookingStatus.PENDING.name,
            "createdAt" to System.currentTimeMillis(),
            "ownerLatitude" to request.ownerLatitude,
            "ownerLongitude" to request.ownerLongitude
        )
        
        db.collection(BOOKINGS_COLLECTION)
            .add(bookingData)
            .addOnSuccessListener { docRef ->
                onSuccess(docRef.id)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to create booking")
            }
    }
    
    fun getPendingBookingsForSitter(
        onSuccess: (List<BookingRequest>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val userId = auth.currentUser?.uid ?: return null
        
        return db.collection(BOOKINGS_COLLECTION)
            .whereEqualTo("sitterId", userId)
            .whereEqualTo("status", BookingStatus.PENDING.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Failed to get bookings")
                    return@addSnapshotListener
                }
                
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        BookingRequest(
                            id = doc.id,
                            ownerId = doc.getString("ownerId") ?: "",
                            ownerName = doc.getString("ownerName") ?: "",
                            sitterId = doc.getString("sitterId") ?: "",
                            sitterName = doc.getString("sitterName") ?: "",
                            petName = doc.getString("petName") ?: "",
                            petSpecies = doc.getString("petSpecies") ?: "",
                            tasks = doc.getString("tasks") ?: "",
                            symptoms = doc.getString("symptoms") ?: "",
                            pickupAddress = doc.getString("pickupAddress") ?: "",
                            destinationAddress = doc.getString("destinationAddress") ?: "",
                            notes = doc.getString("notes") ?: "",
                            status = BookingStatus.valueOf(doc.getString("status") ?: "PENDING"),
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            ownerLatitude = doc.getDouble("ownerLatitude") ?: 0.0,
                            ownerLongitude = doc.getDouble("ownerLongitude") ?: 0.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                onSuccess(bookings)
            }
    }
    
    fun getActiveBookingForOwner(
        onUpdate: (BookingRequest?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val userId = auth.currentUser?.uid ?: return null
        
        return db.collection(BOOKINGS_COLLECTION)
            .whereEqualTo("ownerId", userId)
            .whereIn("status", listOf(BookingStatus.PENDING.name, BookingStatus.ACCEPTED.name, BookingStatus.IN_PROGRESS.name))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Failed to get booking")
                    return@addSnapshotListener
                }
                
                val doc = snapshot?.documents?.firstOrNull()
                if (doc == null) {
                    onUpdate(null)
                    return@addSnapshotListener
                }
                
                try {
                    val booking = BookingRequest(
                        id = doc.id,
                        ownerId = doc.getString("ownerId") ?: "",
                        ownerName = doc.getString("ownerName") ?: "",
                        sitterId = doc.getString("sitterId") ?: "",
                        sitterName = doc.getString("sitterName") ?: "",
                        petName = doc.getString("petName") ?: "",
                        petSpecies = doc.getString("petSpecies") ?: "",
                        tasks = doc.getString("tasks") ?: "",
                        symptoms = doc.getString("symptoms") ?: "",
                        pickupAddress = doc.getString("pickupAddress") ?: "",
                        destinationAddress = doc.getString("destinationAddress") ?: "",
                        notes = doc.getString("notes") ?: "",
                        status = BookingStatus.valueOf(doc.getString("status") ?: "PENDING"),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        ownerLatitude = doc.getDouble("ownerLatitude") ?: 0.0,
                        ownerLongitude = doc.getDouble("ownerLongitude") ?: 0.0
                    )
                    onUpdate(booking)
                } catch (e: Exception) {
                    onUpdate(null)
                }
            }
    }
    
    fun updateBookingStatus(
        bookingId: String,
        status: BookingStatus,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection(BOOKINGS_COLLECTION).document(bookingId)
            .update("status", status.name)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update booking") }
    }
    
    // ==================== MESSAGES ====================
    
    fun sendMessage(
        bookingId: String,
        text: String,
        isFromOwner: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val messageData = hashMapOf(
            "text" to text,
            "isFromOwner" to isFromOwner,
            "timestamp" to System.currentTimeMillis()
        )
        
        db.collection(BOOKINGS_COLLECTION).document(bookingId)
            .collection(MESSAGES_COLLECTION)
            .add(messageData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to send message") }
    }
    
    fun listenToMessages(
        bookingId: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return db.collection(BOOKINGS_COLLECTION).document(bookingId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Failed to get messages")
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id = doc.id,
                            text = doc.getString("text") ?: "",
                            isFromOwner = doc.getBoolean("isFromOwner") ?: true,
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                onUpdate(messages)
            }
    }
}

// Data class for chat messages
data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val isFromOwner: Boolean = true,
    val timestamp: Long = 0L
)

