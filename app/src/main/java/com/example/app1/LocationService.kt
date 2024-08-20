package com.example.app1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        locationClient = LocationClientImpl(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "Location service started")
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        locationClient
            .getLocationUpdates(10000L) // Update interval in milliseconds
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val updatedNotification = createNotification("Location: ($lat, $long)")
                notificationManager.notify(NOTIFICATION_ID, updatedNotification)

                // Send the updated location via Broadcast
                sendLocationUpdate(lat, long)
            }
            .launchIn(serviceScope)
    }

    private fun sendLocationUpdate(lat: String, long: String) {
        val intent = Intent("LOCATION_UPDATE")
        intent.putExtra("latitude", lat)
        intent.putExtra("longitude", long)
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Location Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tracks your location in the background."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String = "Location tracking service is running"): android.app.Notification {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"
        Log.d("LocationService", "Creating notification with content: $contentText")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Tracking Location")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_background) // Use a valid drawable resource
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}