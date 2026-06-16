package wat.edu.pl.projektam.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wat.edu.pl.projektam.MainActivity
import wat.edu.pl.projektam.R
import wat.edu.pl.projektam.data.local.db.dao.SensorDataDao
import wat.edu.pl.projektam.data.local.db.dao.WorkoutPointDao
import wat.edu.pl.projektam.data.local.db.entity.SensorDataEntity
import wat.edu.pl.projektam.data.local.db.entity.WorkoutPointEntity
import wat.edu.pl.projektam.util.Constants
import wat.edu.pl.projektam.util.toFormattedDistance
import wat.edu.pl.projektam.util.toFormattedDuration
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class WorkoutTrackingService : Service(), SensorEventListener {

    @Inject lateinit var pointDao: WorkoutPointDao
    @Inject lateinit var sensorDataDao: SensorDataDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager

    private var workoutId: Long = 0L
    private var startTimeMs: Long = 0L
    private var lastLocation: Location? = null
    private var accumulatedDistanceM: Double = 0.0

    // Detekcja kroków — algorytm peak detection
    private var lastStepTimeMs: Long = 0L
    private var lastMagnitude: Double = 0.0
    private var isRising: Boolean = false
    private var stepCount: Int = 0

    companion object {
        private val _state = MutableStateFlow(TrackingState())
        val state: StateFlow<TrackingState> = _state

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_WORKOUT_ID = "EXTRA_WORKOUT_ID"

        fun start(context: Context, workoutId: Long) {
            Intent(context, WorkoutTrackingService::class.java).also {
                it.action = ACTION_START
                it.putExtra(EXTRA_WORKOUT_ID, workoutId)
                context.startForegroundService(it)
            }
        }

        fun stop(context: Context) {
            Intent(context, WorkoutTrackingService::class.java).also {
                it.action = ACTION_STOP
                context.startService(it)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                workoutId = intent.getLongExtra(EXTRA_WORKOUT_ID, 0L)
                startTimeMs = System.currentTimeMillis()
                startForeground(Constants.NOTIFICATION_ID_WORKOUT, buildNotification())
                startLocationUpdates()
                startAccelerometer()
                _state.value = TrackingState(isTracking = true, workoutId = workoutId)
            }
            ACTION_STOP -> {
                stopTracking()
            }
        }
        return START_STICKY
    }

    // ── GPS ──────────────────────────────────────────────────────────────

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            lastLocation?.let { prev ->
                accumulatedDistanceM += prev.distanceTo(location)
            }
            lastLocation = location
            updateState(location)
            savePoint(location)
        }
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_UPDATE_INTERVAL_MS
        )
            .setMinUpdateDistanceMeters(Constants.LOCATION_MIN_DISPLACEMENT_M)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun savePoint(location: Location) {
        serviceScope.launch {
            pointDao.insert(
                WorkoutPointEntity(
                    workoutId = workoutId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    recordedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // ── Akcelerometr — peak detection ────────────────────────────────────

    private fun startAccelerometer() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        val x = event.values[0].toDouble()
        val y = event.values[1].toDouble()
        val z = event.values[2].toDouble()
        val magnitude = sqrt(x * x + y * y + z * z)

        val stepDetected = detectStep(magnitude)
        if (stepDetected) stepCount++

        serviceScope.launch {
            sensorDataDao.insert(
                SensorDataEntity(
                    workoutId = workoutId,
                    accX = event.values[0],
                    accY = event.values[1],
                    accZ = event.values[2],
                    stepDetected = stepDetected,
                    recordedAt = System.currentTimeMillis()
                )
            )
        }

        updateState(lastLocation)
    }

    // Prosty peak detection: wykrywa przejście magnitude przez próg w górę,
    // z debounce 300ms żeby nie liczyć wielokrotnie tego samego kroku.
    private fun detectStep(magnitude: Double): Boolean {
        val now = System.currentTimeMillis()
        val crossed = !isRising && magnitude > Constants.STEP_DETECTION_THRESHOLD
                && (now - lastStepTimeMs) > 300L
        if (magnitude > lastMagnitude) isRising = true
        if (magnitude < lastMagnitude) isRising = false
        lastMagnitude = magnitude
        if (crossed) lastStepTimeMs = now
        return crossed
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    // ── Stan i powiadomienie ─────────────────────────────────────────────

    private fun updateState(location: Location?) {
        val duration = System.currentTimeMillis() - startTimeMs
        _state.value = TrackingState(
            isTracking = true,
            workoutId = workoutId,
            distanceM = accumulatedDistanceM,
            stepCount = stepCount,
            durationMs = duration,
            currentLocation = location
        )
        updateNotification(duration, accumulatedDistanceM)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_WORKOUT)
        .setSmallIcon(R.drawable.ic_home)
        .setContentTitle(getString(R.string.workout_active))
        .setContentText("00:00  •  0 m")
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun updateNotification(durationMs: Long, distanceM: Double) {
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_WORKOUT)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle(getString(R.string.workout_active))
            .setContentText("${durationMs.toFormattedDuration()}  •  ${distanceM.toFormattedDistance()}")
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(Constants.NOTIFICATION_ID_WORKOUT, notification)
    }

    // ── Czyszczenie ──────────────────────────────────────────────────────

    private fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
        _state.value = TrackingState(isTracking = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
