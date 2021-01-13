package com.example.follower

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.preference.PreferenceManager
import java.util.*

const val CHANNEL_ID = "GPS_CHANNEL"
private const val FOREGROUND_SERVICE_ID = 123
private const val LOCATION_INTERVAL = 500L
private const val LOCATION_DISTANCE = 10f
const val ACTION_TERMINATE = "BackgroundTracker.action_terminate"
const val TRACKING_ID = "sharedPref.trackingId"

class BackgroundTracker : Service() {
    companion object {
        var isTracking = false
    }

    private val binder = LocationServiceBinder()
    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager

    override fun onBind(intent: Intent): IBinder = binder

    private inner class LocationListener : android.location.LocationListener {
        private var lastLocation: Location

        init { lastLocation = Location(LocationManager.GPS_PROVIDER) }

        override fun onLocationChanged(location: Location) {
            val currAddress = Geocoder(this@BackgroundTracker, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
            val prevAddress = Geocoder(this@BackgroundTracker, Locale.getDefault()).getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
            if (currAddress[0].thoroughfare != prevAddress[0].thoroughfare && currAddress[0].featureName != prevAddress[0].featureName){
                lastLocation = location
                FlightRecorder.i { "${System.currentTimeMillis()}: Location Changed. lat:${location.latitude}, long:${location.longitude}" }
            }
        }

        override fun onProviderDisabled(provider: String) { FlightRecorder.w { "onProviderDisabled: $provider" } }
        override fun onProviderEnabled(provider: String) { FlightRecorder.w {"onProviderEnabled: $provider" } }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) { FlightRecorder.w { "onStatusChanged: $status" } }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if(intent.action == ACTION_TERMINATE) {
            stopSelf()
        } else {
            startTracking()
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        FlightRecorder.i { "${javaClass.simpleName} onCreate()" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))
        val notification = Notification.Builder(applicationContext, CHANNEL_ID).setAutoCancel(false).build()
        startForeground(FOREGROUND_SERVICE_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putBoolean(TRACKING_ID, false).apply()
        isTracking = false
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (ex: Exception) {
                FlightRecorder.i { "Failed to remove location listeners" }
                FlightRecorder.e(stackTrace = ex.stackTrace)
            }
        }
    }

    private fun startTracking() {
        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener()
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener)
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putBoolean(TRACKING_ID, true).apply()
        } catch (ex: SecurityException) {
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putBoolean(TRACKING_ID, false).apply()
            FlightRecorder.i { "Failed to request location update" }
            FlightRecorder.e(stackTrace = ex.stackTrace)
        } catch (ex: IllegalArgumentException) {
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putBoolean(TRACKING_ID, false).apply()
            FlightRecorder.i { "GPS provider does not exist (${ex.localizedMessage})" }
            FlightRecorder.e(stackTrace = ex.stackTrace)
        }
    }

    inner class LocationServiceBinder : Binder() {
        val service: BackgroundTracker
            get() = this@BackgroundTracker
    }
}