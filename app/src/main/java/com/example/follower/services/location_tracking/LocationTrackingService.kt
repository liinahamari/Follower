package com.example.follower.services.location_tracking

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import com.example.follower.BuildConfig
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.db.entities.Track
import com.example.follower.db.entities.toWayPoint
import com.example.follower.ext.tryLogging
import com.example.follower.helper.CustomToast.errorToast
import com.example.follower.helper.CustomToast.successToast
import com.example.follower.helper.FlightRecorder
import com.example.follower.interactors.SaveTrackResult
import com.example.follower.interactors.TrackInteractor
import com.example.follower.screens.tracking_control.UploadTrackInteractor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val CHANNEL_ID = "GPS_CHANNEL"
private const val FOREGROUND_SERVICE_ID = 123
const val ACTION_START_TRACKING = "BackgroundTracker.action_start_tracking"
const val ACTION_DISCARD_TRACK = "BackgroundTracker.action_discard_track"
const val ACTION_RENAME_TRACK_AND_STOP_TRACKING = "BackgroundTracker.action_rename_track"
const val ARG_AUTO_SAVE = "BackgroundTracker.arg_auto_save"

class LocationTrackingService : Service() {
    private val notification by lazy {
        Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentText("tracking..." /*todo*/)
            .setAutoCancel(false)
            .build()
    }

    private val disposable = CompositeDisposable()
    private val syncDisposable = CompositeDisposable()
    var traceBeginningTime: Long? = null
    var isTrackEmpty = true

    @Inject lateinit var prefInteractor: LocationPreferenceInteractor
    @Inject lateinit var logger: FlightRecorder
    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var trackInteractor: TrackInteractor
    @Inject lateinit var uploadTrackInteractor: UploadTrackInteractor

    private val locationListener = LocationListener()
    private val binder = LocationServiceBinder()
    val isTracking = BehaviorSubject.createDefault(false)
    val wayPointsCounter = BehaviorSubject.createDefault(0)

    override fun onBind(intent: Intent): IBinder = binder

    inner class LocationListener : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            logger.i { "${System.currentTimeMillis()}: Location Changed. lat:${location.latitude}, lon:${location.longitude}" }
            disposable += trackInteractor.saveWayPoint(location.toWayPoint(traceBeginningTime!!)).subscribe {
                wayPointsCounter.onNext(wayPointsCounter.value!!.inc())
                if (isTrackEmpty) {
                    isTrackEmpty = false
                }
            }
        }

        override fun onProviderDisabled(provider: String) = logger.w { "onProviderDisabled: $provider" } /*todo: handle user's geolocation permission revoking*/
        override fun onProviderEnabled(provider: String) = logger.w { "onProviderEnabled: $provider" }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = Unit /* Cause DEPRECATED */
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_RENAME_TRACK_AND_STOP_TRACKING -> renameTrackAndStopTracking(intent.extras?.getCharSequence(ARG_AUTO_SAVE, null))
            ACTION_START_TRACKING -> startTracking()
            ACTION_DISCARD_TRACK -> discardTrack()
        }
        return START_STICKY
    }

    private fun discardTrack() {
        disposable += trackInteractor.deleteTrack(traceBeginningTime!!)/*todo delete on server*/
            .subscribe({ stopTracking() }, { stopTracking() })
    }

    override fun onCreate() {
        (application as FollowerApp).appComponent.inject(this)
        logger.i { "${javaClass.simpleName} onCreate()" }
    }

    override fun onDestroy() {
        /* to prevent leaks dispose all the subscriptions here (in case system kills service to free the resources)*/
        logger.d { "${javaClass.simpleName} onDestroy()" }
        stopForeground(true)
        isTracking.onNext(false)
        disposable.clear()
        syncDisposable.clear()
        kotlin.runCatching { locationManager.removeUpdates(locationListener) }
    }

    private fun stopTracking() {
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (ex: Exception) {
                logger.e(label = "Failed to remove location listeners", stackTrace = ex.stackTrace)
            } finally {
                isTracking.onNext(false) /* ? */
                syncDisposable.clear()
                wayPointsCounter.onNext(0)
            }
        }
        stopSelf()
    }

    private fun renameTrackAndStopTracking(title: CharSequence?) {
        title?.let {
            disposable += trackInteractor.renameTrack(Track(traceBeginningTime!!, it.toString()))
                .subscribe { saveResult ->
                    when (saveResult) {
                        is SaveTrackResult.Success -> successToast(getString(R.string.toast_track_saved)) /*todo check availability of toasts from service in latest versions*/
                        is SaveTrackResult.DatabaseCorruptionError -> errorToast(getString(R.string.error_couldnt_save_track))
                    }
                }
        }
        uploadTrackInteractor.uploadTrack(traceBeginningTime!!) /*process needed to be reflected in UI*/
        stopTracking()
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
            isTrackEmpty = true
            traceBeginningTime = System.currentTimeMillis()

            disposable += trackInteractor.saveTrack(Track(traceBeginningTime!!, traceBeginningTime!!.toString())).subscribe() /*what if fails?*/

            syncDisposable += (
                    if (BuildConfig.DEBUG)
                        Observable.interval(15, TimeUnit.SECONDS) else Observable.interval(10, TimeUnit.MINUTES)
                    )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    uploadTrackInteractor.uploadTrack(traceBeginningTime!!)
                }
        } catch (ex: SecurityException) {
            isTracking.onNext(false)
            stopSelf()
            logger.e(label = "Failed to request location updates", stackTrace = ex.stackTrace)
        } catch (ex: IllegalArgumentException) {
            stopSelf()
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