package com.example.app1.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.app1.MainActivity
import com.example.app1.R
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private val notifiedLandmarks = mutableSetOf<String>()

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
        when (intent?.action) {
            ACTION_START -> {
                Log.d("LocationService", "Service started")

                // Create notification
                val notification = createNotification()

                // Start foreground service with the notification
                startForeground(NOTIFICATION_ID, notification)

                // Start location updates after starting foreground
                startLocationUpdates()
            }
            ACTION_STOP -> {
                Log.d("LocationService", "Service stopped")
                stop()
            }
            ACTION_FIND_NEARBY -> {
                Log.d("LocationService", "Finding nearby landmarks")
                startLocationUpdates(nearby = true)
            }
        }

        return START_STICKY
    }

    private fun startLocationUpdates(nearby: Boolean = false) {
        locationClient.getLocationUpdates(10000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                Log.d("LocationService", "Location: ${location.latitude}, ${location.longitude}")
                sendLocationUpdate(location.latitude, location.longitude)
                if (nearby) {
                    checkProximityToLandmarks(location.latitude, location.longitude)
                }
            }.launchIn(serviceScope)
    }

    private fun sendLocationUpdate(lat: Double, long: Double) {
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            putExtra(EXTRA_LOCATION_LATITUDE, lat)
            putExtra(EXTRA_LOCATION_LONGITUDE, long)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun checkProximityToLandmarks(latitude: Double, longitude: Double) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("landmarks").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val geoPoint = document.getGeoPoint("location")
                    geoPoint?.let {
                        val distance = calculateDistance(latitude, longitude, it.latitude, it.longitude)
                        if (distance <= 100 && !notifiedLandmarks.contains(document.id)) {
                            val eventName = document.getString("eventName") ?: "Landmark"

                            sendNearbyLandmarkNotification(document.id, eventName)
                            notifiedLandmarks.add(document.id)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("LocationService", "Error fetching landmarks", e)
            }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // Correct Earth radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun sendNearbyLandmarkNotification(landmarkId: String, landmarkName: String) {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("landmarkId", landmarkId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Landmark Nearby")
            .setContentText("You are near $landmarkName!")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NEARBY_LANDMARK_NOTIFICATION_ID, notification)
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when you are near a landmark."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Location Tracking")
            .setContentText("Location tracking service is running in the background")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_FIND_NEARBY = "ACTION_FIND_NEARBY"
        const val ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE"
        const val EXTRA_LOCATION_LATITUDE = "EXTRA_LOCATION_LATITUDE"
        const val EXTRA_LOCATION_LONGITUDE = "EXTRA_LOCATION_LONGITUDE"
        private const val NOTIFICATION_ID = 1
        private const val NEARBY_LANDMARK_NOTIFICATION_ID = 2
    }
}
