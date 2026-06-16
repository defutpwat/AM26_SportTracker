package wat.edu.pl.projektam

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import wat.edu.pl.projektam.util.Constants

@HiltAndroidApp
class SportTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Kanał dla powiadomienia foreground service (aktywny trening)
            NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_WORKOUT,
                "Aktywny trening",
                NotificationManager.IMPORTANCE_LOW  // LOW = bez dźwięku, ale widoczny
            ).also { manager.createNotificationChannel(it) }

            // Kanał dla powiadomień PUSH z FCM
            NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_PUSH,
                "Powiadomienia",
                NotificationManager.IMPORTANCE_DEFAULT
            ).also { manager.createNotificationChannel(it) }
        }
    }
}
