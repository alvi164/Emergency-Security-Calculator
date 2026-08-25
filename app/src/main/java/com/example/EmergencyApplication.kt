package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class EmergencyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Recording / Active Service Channel (HIGH/DEFAULT importance per Android foreground policies)
            val serviceChannel = NotificationChannel(
                CHANNEL_EMERGENCY_SERVICE,
                "Emergency Safety Monitor",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows persistent status while emergency shake detection and video recording are active"
                setShowBadge(true)
                enableVibration(false)
            }

            // Upload / Status Notifications Channel
            val uploadChannel = NotificationChannel(
                CHANNEL_EVIDENCE_UPLOAD,
                "Evidence Upload Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for automatic cloud upload and synchronization status"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(uploadChannel)
        }
    }

    companion object {
        const val CHANNEL_EMERGENCY_SERVICE = "emergency_service_channel"
        const val CHANNEL_EVIDENCE_UPLOAD = "evidence_upload_channel"
    }
}
