package com.example.follower

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import java.util.*
import javax.inject.Inject

const val CHANNEL_ID = "GPS_CHANNEL"
private const val FOREGROUND_SERVICE_ID = 123
private const val LOCATION_INTERVAL = 500L
private const val LOCATION_DISTANCE = 10f
const val ACTION_TERMINATE = "BackgroundTracker.action_terminate"
const val TRACKING_ID = "sharedPref.trackingId"

class LocationTrackingService : Service() {
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var logger: FlightRecorder
    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager

    override fun onBind(intent: Intent): IBinder? = null

    private inner class LocationListener : android.location.LocationListener {
        private var lastLocation: Location

        init { lastLocation = Location(LocationManager.GPS_PROVIDER) }

        override fun onLocationChanged(location: Location) {
//            val currAddress = Geocoder(this@LocationTrackingService, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
//            val prevAddress = Geocoder(this@LocationTrackingService, Locale.getDefault()).getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
//            if (currAddress[0].thoroughfare != prevAddress[0].thoroughfare && currAddress[0].featureName != prevAddress[0].featureName){
                lastLocation = location
                logger.i { "${System.currentTimeMillis()}: Location Changed. lat:${location.latitude}, long:${location.longitude}" }
//            }
        }

        override fun onProviderDisabled(provider: String) { logger.w { "onProviderDisabled: $provider" } }
        override fun onProviderEnabled(provider: String) { logger.w {"onProviderEnabled: $provider" } }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) { logger.w { "onStatusChanged: $status" } }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent.action == ACTION_TERMINATE) {
            stopSelf()
        } else {
            startTracking()
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        (application as FollowerApp).appComponent.inject(this)

        logger.i { "${javaClass.simpleName} onCreate()" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))
        startForeground(FOREGROUND_SERVICE_ID, Notification.Builder(applicationContext, CHANNEL_ID)
                                                    .setContentText("tracking...")
                                                    .setAutoCancel(false)
                                                    .build())
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPrefs.edit().putBoolean(TRACKING_ID, false).apply()
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (ex: Exception) {
                logger.i { "Failed to remove location listeners" }
                logger.e(stackTrace = ex.stackTrace)
            }
        }
    }

    private fun startTracking() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener()
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener)
            sharedPrefs.edit().putBoolean(TRACKING_ID, true).apply()
        } catch (ex: SecurityException) {
            sharedPrefs.edit().putBoolean(TRACKING_ID, false).apply()
            logger.i { "Failed to request location update" }
            logger.e(stackTrace = ex.stackTrace)
        } catch (ex: IllegalArgumentException) {
            sharedPrefs.edit().putBoolean(TRACKING_ID, false).apply()
            logger.i { "GPS provider does not exist (${ex.localizedMessage})" }
            logger.e(stackTrace = ex.stackTrace)
        }
    }
}