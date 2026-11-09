package dcrustm.ecell.mobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp
import dcrustm.ecell.mobile.messaging.NOTIFICATION_CHANNEL_ID

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Announcements & updates",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "New announcements, schedule changes, and complaint responses"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
