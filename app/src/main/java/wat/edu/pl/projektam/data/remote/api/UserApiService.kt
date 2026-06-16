package wat.edu.pl.projektam.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import wat.edu.pl.projektam.data.remote.dto.UpdateProfileRequest
import wat.edu.pl.projektam.data.remote.dto.UserResponse

interface UserApiService {

    @GET("api/users/me")
    suspend fun getProfile(): Response<UserResponse>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @POST("api/notifications")
    suspend fun sendNotification(@Body body: Map<String, String>): Response<Unit>
}
