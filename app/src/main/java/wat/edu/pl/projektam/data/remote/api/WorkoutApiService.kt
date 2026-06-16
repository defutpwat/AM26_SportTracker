package wat.edu.pl.projektam.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import wat.edu.pl.projektam.data.remote.dto.WorkoutPointRequest
import wat.edu.pl.projektam.data.remote.dto.WorkoutRequest
import wat.edu.pl.projektam.data.remote.dto.WorkoutResponse

interface WorkoutApiService {

    @GET("api/workouts")
    suspend fun getWorkouts(): Response<List<WorkoutResponse>>

    @GET("api/workouts/{id}")
    suspend fun getWorkout(@Path("id") id: Long): Response<WorkoutResponse>

    @POST("api/workouts")
    suspend fun createWorkout(@Body request: WorkoutRequest): Response<WorkoutResponse>

    @POST("api/workouts/{id}/points")
    suspend fun addPoints(
        @Path("id") workoutId: Long,
        @Body points: List<WorkoutPointRequest>
    ): Response<Unit>

    @DELETE("api/workouts/{id}")
    suspend fun deleteWorkout(@Path("id") id: Long): Response<Unit>
}
