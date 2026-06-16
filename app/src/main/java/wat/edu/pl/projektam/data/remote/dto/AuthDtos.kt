package wat.edu.pl.projektam.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class AuthResponse(
    val token: String,
    val email: String,
    val role: String,
    val displayName: String
)

data class FcmTokenRequest(
    val fcmToken: String
)
