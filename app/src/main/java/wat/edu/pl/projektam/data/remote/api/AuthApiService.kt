package wat.edu.pl.projektam.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import wat.edu.pl.projektam.data.remote.dto.AuthResponse
import wat.edu.pl.projektam.data.remote.dto.FcmTokenRequest
import wat.edu.pl.projektam.data.remote.dto.LoginRequest
import wat.edu.pl.projektam.data.remote.dto.RegisterRequest

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @PUT("api/auth/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<Unit>
}
