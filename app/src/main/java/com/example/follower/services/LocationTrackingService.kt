package com.example.follower.services

import android.app.Notification
import android.app.Service
import android.content.Intent
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
const val ACTION_START_TRACKING = "BackgroundTracker.action_start_tracking"
const val ACTION_STOP_TRACKING = "BackgroundTracker.action_stop_tracking"

class LocationTrackingService : Service() {
    val wayPoints = mutableListOf<WayPoint>()
    var traceBeginningTime: Long? = null

    @Inject lateinit var prefInteractor: LocationPreferenceInteractor
    @Inject lateinit var logger: FlightRecorder

    private lateinit var locationListener: LocationListener
    private lateinit var locationManager: LocationManager
    private val binder = LocationServiceBinder()
    val isTracking = BehaviorSubject.createDefault(false)

    override fun onBind(intent: Intent): IBinder = binder

    private inner class LocationListener : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            wayPoints.add(WayPoint(1L, location.provider, longitude = location.longitude, latitude = location.latitude, time = System.currentTimeMillis()))
            logger.i { "${System.currentTimeMillis()}: Location Changed. lat:${location.latitude}, long:${location.longitude}" }
        }

        override fun onProviderDisabled(provider: String) = logger.w { "onProviderDisabled: $provider" }
        override fun onProviderEnabled(provider: String) = logger.w { "onProviderEnabled: $provider" }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = logger.w { "onStatusChanged: $status" }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_STOP_TRACKING -> stopSelf()
            ACTION_START_TRACKING -> startTracking()
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

        val timeUpdateInterval = (prefInteractor.getTimeIntervalBetweenUpdates()
            .blockingGet() as GetTimeIntervalResult.Success).timeInterval

        val distanceBetweenUpdates = (prefInteractor.getDistanceBetweenUpdates()
            .blockingGet() as GetDistanceResult.Success).distanceBetweenUpdates

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeUpdateInterval, distanceBetweenUpdates, locationListener)
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

    /*FIXME: LeakCanary concerns about $this.
    * https://github.com/harshvardhanrastogi/leak-proof-service
    * */
    inner class LocationServiceBinder : Binder() {
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }
}