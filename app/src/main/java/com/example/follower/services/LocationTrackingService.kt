package com.example.follower.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import com.example.follower.FollowerApp
import com.example.follower.db.entities.WayPoint
import com.example.follower.helper.FlightRecorder
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

const val CHANNEL_ID = "GPS_CHANNEL"
private const val FOREGROUND_SERVICE_ID = 123
private const val LOCATION_INTERVAL = 500L
private const val LOCATION_DISTANCE = 10f
const val ACTION_TERMINATE = "BackgroundTracker.action_terminate"

class LocationTrackingService : Service() {
    @Volatile var wayPoints = mutableListOf<WayPoint>()
    @Volatile var traceBeginningTime: Long? = null
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var logger: FlightRecorder
    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager
    private val binder = LocationServiceBinder()
    val isTracking = BehaviorSubject.createDefault(false)

    override fun onBind(intent: Intent): IBinder = binder

    private inner class LocationListener : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
//            val currAddress = Geocoder(this@LocationTrackingService, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
//            val prevAddress = Geocoder(this@LocationTrackingService, Locale.getDefault()).getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
//            if (currAddress[0].thoroughfare != prevAddress[0].thoroughfare && currAddress[0].featureName != prevAddress[0].featureName){
            wayPoints.add(WayPoint(1L, location.provider, longitude = location.longitude, latitude = location.latitude, time = System.currentTimeMillis()))

            logger.i { "${System.currentTimeMillis()}: Location Changed. lat:${location.latitude}, long:${location.longitude}" }
        }

        override fun onProviderDisabled(provider: String) = logger.w { "onProviderDisabled: $provider" }
        override fun onProviderEnabled(provider: String) = logger.w { "onProviderEnabled: $provider" }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = logger.w { "onStatusChanged: $status" }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent.action == ACTION_TERMINATE) {
            stopSelf()
        } else {
            startTracking()
        }
        return START_STICKY
    }

    override fun onCreate() {
        (application as FollowerApp).appComponent.inject(this)

        logger.i { "${javaClass.simpleName} onCreate()" }
        startForeground(
            FOREGROUND_SERVICE_ID, Notification.Builder(applicationContext, CHANNEL_ID)
                .setContentText("tracking...")
                .setAutoCancel(false)
                .build()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.d { "${javaClass.simpleName} onDestroy()" }
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (ex: Exception) {
                logger.e(label = "Failed to remove location listeners", stackTrace = ex.stackTrace)
            } finally {
                isTracking.onNext(false)
            }
        }
    }

    private fun startTracking() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener()
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener)
            isTracking.onNext(true)
            traceBeginningTime = System.currentTimeMillis()
        } catch (ex: SecurityException) {
            isTracking.onNext(false)
            logger.e(label = "Failed to request location update", stackTrace = ex.stackTrace)
        } catch (ex: IllegalArgumentException) {
            isTracking.onNext(false)
            logger.e(label = "GPS provider does not exist (${ex.localizedMessage})", stackTrace = ex.stackTrace)
        }
    }

    /*FIXME: shitty thing. TODO: try Broadcasts instead*/
    inner class LocationServiceBinder : Binder() {
        val service: LocationTrackingService
            get() = this@LocationTrackingService
    }
}