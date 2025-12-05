package com.example.onpaw

// Pet data model (already exists in PetProfilesActivity, consolidating here)
data class Pet(
    var name: String = "",
    var species: String = "",
    var age: Int = 0,
    var note: String = "",
    var icon: Int = R.drawable.paw
)

// User data model with location
data class User(
    var name: String = "John",
    var email: String = "johnsmith@gmail.com",
    var phone: String = "123-456-7890",
    var address: String = "",
    var latitude: Double = 40.1020,  // ISR at UIUC coordinates
    var longitude: Double = -88.2282,
    var password: String = "",
    var isLoggedIn: Boolean = false,
    var petList: MutableList<Pet> = mutableListOf()
)

val userList: MutableList<User> = mutableListOf(
    User(
        name = "Test User",
        email = "1234@email.com",
        phone = "123-456-7890",
        password = "1234"
    )
)
val user = User()

fun logOutUser() {
    user.name = ""
    user.email = ""
    user.phone = ""
    user.address = ""
    user.password = ""
    user.isLoggedIn = false
    user.petList = mutableListOf()
}

fun deleteUser(delUser : User) {
    val idx = userList.indexOfFirst { it.email == delUser.email }
    if (idx != -1) {
        userList.removeAt(idx)
    }
    logOutUser()
}



// Pet Sitter data model
data class PetSitter(
    var id: Int,
    var name: String,
    var email: String,
    var phone: String,
    var address: String,
    var latitude: Double,
    var longitude: Double,
    var rating: Double,
    var hourlyRate: Double,
    var bio: String,
    var availability: String,
    var petTypes: List<String>, // e.g., ["Dog", "Cat", "Bird"]
    var yearsExperience: Int,
    var profileImage: Int = R.drawable.account
)

//var petList: MutableList<Pet> = mutableListOf()

// Fake pet sitters data
val petSitters: MutableList<PetSitter> = mutableListOf(
    PetSitter(
        id = 1,
        name = "John Smith",
        email = "john.smith@email.com",
        phone = "217-555-0101",
        address = "Ikenberry Commons, Champaign, IL",
        latitude = 40.1070,
        longitude = -88.2247,
        rating = 4.8,
        hourlyRate = 25.0,
        bio = "Experienced pet sitter with 5 years of caring for dogs and cats. CPR certified.",
        availability = "Weekdays 9AM-6PM",
        petTypes = listOf("Dog", "Cat"),
        yearsExperience = 5
    ),
    PetSitter(
        id = 2,
        name = "Sarah Johnson",
        email = "sarah.j@email.com",
        phone = "217-555-0102",
        address = "Illini Union, Urbana, IL",
        latitude = 40.1091,
        longitude = -88.2272,
        rating = 4.9,
        hourlyRate = 30.0,
        bio = "Professional dog walker and pet sitter. Specialized in senior pets and puppies.",
        availability = "7 days a week, flexible hours",
        petTypes = listOf("Dog", "Cat", "Rabbit"),
        yearsExperience = 7
    ),
    PetSitter(
        id = 3,
        name = "Michael Chen",
        email = "mchen@email.com",
        phone = "217-555-0103",
        address = "Grainger Library, Urbana, IL",
        latitude = 40.1125,
        longitude = -88.2268,
        rating = 4.7,
        hourlyRate = 22.0,
        bio = "Love all animals! Have two cats and a dog of my own. Comfortable with all pet types.",
        availability = "Evenings and weekends",
        petTypes = listOf("Dog", "Cat", "Bird", "Hamster"),
        yearsExperience = 3
    ),
    PetSitter(
        id = 4,
        name = "Emily Rodriguez",
        email = "emily.r@email.com",
        phone = "217-555-0104",
        address = "Main Quad, Urbana, IL",
        latitude = 40.1075,
        longitude = -88.2280,
        rating = 5.0,
        hourlyRate = 35.0,
        bio = "Certified veterinary technician offering premium pet care. 10 years experience.",
        availability = "By appointment",
        petTypes = listOf("Dog", "Cat", "Reptile", "Bird"),
        yearsExperience = 10
    ),
    PetSitter(
        id = 5,
        name = "David Park",
        email = "dpark@email.com",
        phone = "217-555-0105",
        address = "CRCE, Champaign, IL",
        latitude = 40.1018,
        longitude = -88.2360,
        rating = 4.6,
        hourlyRate = 20.0,
        bio = "College student with flexible schedule. Great with energetic dogs and playful cats!",
        availability = "Afternoons and weekends",
        petTypes = listOf("Dog", "Cat"),
        yearsExperience = 2
    )
)

object ChatStore {
    val messageHistory = mutableMapOf<String, MutableList<ChatActivity.Message>>()
}

// Booking status enum
enum class BookingStatus {
    PENDING,      // Request sent, waiting for sitter to accept
    ACCEPTED,     // Sitter accepted, on their way
    IN_PROGRESS,  // Sitter is with the pet
    COMPLETED,    // Service completed
    CANCELLED     // Booking was cancelled
}

// Booking request data model
data class BookingRequest(
    var id: String = "",
    var ownerId: String = "",
    var ownerName: String = "",
    var sitterId: String = "",
    var sitterName: String = "",
    var petName: String = "",
    var petSpecies: String = "",
    var tasks: String = "",
    var symptoms: String = "",
    var pickupAddress: String = "",
    var destinationAddress: String = "",
    var notes: String = "",
    var status: BookingStatus = BookingStatus.PENDING,
    var createdAt: Long = System.currentTimeMillis(),
    var ownerLatitude: Double = 0.0,
    var ownerLongitude: Double = 0.0
)

// Current active booking (for fake data mode)
var currentBooking: BookingRequest? = null

// Current matched sitter (for fake data mode)
var matchedSitter: PetSitter? = null

// Function to get the closest pet sitters using Haversine formula
fun getClosestPetSitters(userLat: Double, userLon: Double, limit: Int = 5): List<PetSitter> {
    return petSitters.sortedBy { sitter ->
        calculateDistanceKm(userLat, userLon, sitter.latitude, sitter.longitude)
    }.take(limit)
}

// Haversine formula for accurate distance calculation
fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Earth radius in kilometers
    
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    
    return earthRadius * c
}

// Find closest available sitter (used by LoadingActivity)
fun findClosestAvailableSitter(userLat: Double, userLon: Double): PetSitter? {
    return getClosestPetSitters(userLat, userLon, 1).firstOrNull()
}

