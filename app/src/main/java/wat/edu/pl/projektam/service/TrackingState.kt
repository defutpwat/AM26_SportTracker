package wat.edu.pl.projektam.service

import android.location.Location

data class TrackingState(
    val isTracking: Boolean = false,
    val workoutId: Long = 0L,
    val distanceM: Double = 0.0,
    val stepCount: Int = 0,
    val durationMs: Long = 0L,
    val currentLocation: Location? = null
)
