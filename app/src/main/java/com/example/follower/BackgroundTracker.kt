package com.example.follower

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import java.io.File

private const val CHANNEL_ID = "GPS_CHANNEL"
private const val LOCATION_INTERVAL = 500
private const val LOCATION_DISTANCE = 10
private const val TAG = "BackgroundService"
private const val DEBUG_LOGS_DIR = "FlightRecordings"
private const val DEBUG_LOGS_STORAGE_FILE_NAME = "tape.log"

class BackgroundTracker : Service() {
    private val logFile = File(File(filesDir, DEBUG_LOGS_DIR).apply {
        if (exists().not()) {
            mkdir()
        }
    }, DEBUG_LOGS_STORAGE_FILE_NAME)
    private val logger = FlightRecorder(logFile)
    private val binder = LocationServiceBinder()
    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager

    override fun onBind(intent: Intent): IBinder = binder

    private inner class LocationListener : android.location.LocationListener {
        private lateinit var lastLocation: Location

        override fun onLocationChanged(location: Location) {
            lastLocation = location
            logger.i { "Location Changed: $location" }
        }

        override fun onProviderDisabled(provider: String) { logger.w { "onProviderDisabled: $provider" } }
        override fun onProviderEnabled(provider: String) { logger.w {"onProviderEnabled: $provider" } }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) { logger.w { "onStatusChanged: $status" } }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        logger.i { "${javaClass.simpleName} onCreate()" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))
        val notification = Notification.Builder(applicationContext, CHANNEL_ID).setAutoCancel(false).build()
        startForeground(12345678, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (ex: Exception) {
                logger.i { "Failed to remove location listeners" }
                logger.e(stackTrace = ex.stackTrace)
            }
        }
    }

    fun startTracking() {
        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener()
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE.toFloat(), locationListener)
        } catch (ex: SecurityException) {
            logger.i { "Failed to request location update" }
            logger.e(stackTrace = ex.stackTrace)
        } catch (ex: IllegalArgumentException) {
            logger.i { "GPS provider does not exist (${ex.localizedMessage})" }
            logger.e(stackTrace = ex.stackTrace)
        }
    }

    fun stopTracking() = onDestroy()

    inner class LocationServiceBinder : Binder() {
        val service: BackgroundTracker
            get() = this@BackgroundTracker
    }
}