package wat.edu.pl.projektam.data.remote.dto

data class WorkoutRequest(
    val workoutType: String,
    val startedAt: String,
    val endedAt: String,
    val distanceM: Double,
    val stepCount: Int,
    val notes: String? = null
)

data class WorkoutResponse(
    val id: Long,
    val workoutType: String,
    val startedAt: String,
    val endedAt: String,
    val distanceM: Double,
    val stepCount: Int,
    val notes: String?
)

data class WorkoutPointRequest(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val recordedAt: String
)

data class UserResponse(
    val id: Long,
    val email: String,
    val displayName: String,
    val role: String
)

data class UpdateProfileRequest(
    val displayName: String
)
