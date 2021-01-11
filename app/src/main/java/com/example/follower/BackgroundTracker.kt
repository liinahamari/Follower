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
import android.util.Log

private const val LOCATION_INTERVAL = 500
private const val LOCATION_DISTANCE = 10
private const val TAG = "BackgroundService"

class BackgroundTracker : Service() {
    private val binder = LocationServiceBinder()
    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager

    override fun onBind(intent: Intent): IBinder = binder

    private inner class LocationListener : android.location.LocationListener {
        private lateinit var lastLocation: Location

        override fun onLocationChanged(location: Location) {
            lastLocation = location
            Log.i(javaClass.simpleName, "LocationChanged: $location")
        }

        override fun onProviderDisabled(provider: String) { Log.e(javaClass.simpleName, "onProviderDisabled: $provider") }
        override fun onProviderEnabled(provider: String) { Log.e(javaClass.simpleName, "onProviderEnabled: $provider") }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) { Log.e(javaClass.simpleName, "onStatusChanged: $status") }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        Log.i(TAG, "onCreate")
        startForeground(12345678, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (ex: Exception) {
                Log.i(TAG, "fail to remove location listners, ignore", ex)
            }
        }
    }

    fun startTracking() {
        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener()
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE.toFloat(), locationListener)
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.localizedMessage);
        }
    }

    fun stopTracking() = onDestroy()

    private val notification: Notification
        get() {
            val channel = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            val builder = Notification.Builder(applicationContext, "channel_01").setAutoCancel(true)
            return builder.build()
        }

    inner class LocationServiceBinder : Binder() {
        val service: BackgroundTracker
            get() = this@BackgroundTracker
    }
}