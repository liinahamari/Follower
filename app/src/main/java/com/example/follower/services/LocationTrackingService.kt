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
import com.example.follower.db.entities.toWayPoint
import com.example.follower.helper.FlightRecorder
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

const val CHANNEL_ID = "GPS_CHANNEL"
private const val FOREGROUND_SERVICE_ID = 123
const val ACTION_START_TRACKING = "BackgroundTracker.action_start_tracking"
const val ACTION_STOP_TRACKING = "BackgroundTracker.action_stop_tracking"
const val ARG_AUTO_SAVE = "BackgroundTracker.arg_auto_save"

class LocationTrackingService : Service() {
    private val notification by lazy {
        Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentText("tracking..." /*todo*/)
            .setAutoCancel(false)
            .build()
    }

    val wayPoints = mutableListOf<WayPoint>()
    var traceBeginningTime: Long? = null

    @Inject lateinit var prefInteractor: LocationPreferenceInteractor
    @Inject lateinit var logger: FlightRecorder
    @Inject lateinit var locationManager: LocationManager

    private val locationListener = LocationListener()
    private val binder = LocationServiceBinder()
    val isTracking = BehaviorSubject.createDefault(false)

    override fun onBind(intent: Intent): IBinder = binder

    inner class LocationListener : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            wayPoints.add(location.toWayPoint(traceBeginningTime!!))
            logger.i { "${System.currentTimeMillis()}: Location Changed. lat:${location.latitude}, long:${location.longitude}" }
        }

        override fun onProviderDisabled(provider: String) = logger.w { "onProviderDisabled: $provider" } /*todo: handle user's geolocation permission revoking*/
        override fun onProviderEnabled(provider: String) = logger.w { "onProviderEnabled: $provider" }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = Unit
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_STOP_TRACKING -> stopTracking()
            ACTION_START_TRACKING -> startTracking()
        }
        return START_STICKY
    }

    override fun onCreate() {
        (application as FollowerApp).appComponent.inject(this)

        logger.i { "${javaClass.simpleName} onCreate()" }
    }

    override fun onDestroy() = logger.d { "${javaClass.simpleName} onDestroy()" }.also { isTracking.onNext(false) }

    private fun stopTracking() {
        stopForeground(true)/* ? */
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (ex: Exception) {
                logger.e(label = "Failed to remove location listeners", stackTrace = ex.stackTrace)
            } finally {
                isTracking.onNext(false)
            }
        }
        stopSelf()
    }

    private fun startTracking() {
        startForeground(FOREGROUND_SERVICE_ID, notification)

        val timeUpdateInterval = (prefInteractor.getTimeIntervalBetweenUpdates()
            .blockingGet() as GetTimeIntervalResult.Success).timeInterval

        val distanceBetweenUpdates = (prefInteractor.getDistanceBetweenUpdates()
            .blockingGet() as GetDistanceResult.Success).distanceBetweenUpdates

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeUpdateInterval, distanceBetweenUpdates, locationListener)
            isTracking.onNext(true)
            traceBeginningTime = System.currentTimeMillis()

            /*todo time calculation - is it not too old?*/
            (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))?.let { initLocation ->
                wayPoints.add(initLocation.toWayPoint(traceBeginningTime!!))
            }
        } catch (ex: SecurityException) {
            isTracking.onNext(false)
            stopSelf()
            stopForeground(true)
            logger.e(label = "Failed to request location updates", stackTrace = ex.stackTrace)
        } catch (ex: IllegalArgumentException) {
            stopSelf()
            stopForeground(true)
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