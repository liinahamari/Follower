/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.follower.services.location_tracking

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseService
import dev.liinahamari.follower.base.FOREGROUND_ID_LOCATION_TRACKING
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.toWayPoint
import dev.liinahamari.follower.ext.appComponent
import dev.liinahamari.follower.ext.toReadableDate
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.helper.CustomToast.successToast
import dev.liinahamari.follower.helper.delegates.RxSubscriptionDelegateImpl
import dev.liinahamari.follower.helper.delegates.RxSubscriptionsDelegate
import dev.liinahamari.follower.interactors.SaveTrackResult
import dev.liinahamari.follower.interactors.TrackInteractor
import dev.liinahamari.follower.model.TrackMode
import dev.liinahamari.follower.screens.tracking_control.UploadTrackInteractor
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val ACTION_START_TRACKING = "BackgroundTracker.action_start_tracking"
const val ACTION_DISCARD_TRACK = "BackgroundTracker.action_discard_track"
const val ACTION_RENAME_TRACK_AND_STOP_TRACKING = "BackgroundTracker.action_rename_track"
const val ARG_AUTO_SAVE = "BackgroundTracker.arg_auto_save"
const val ARG_TRACK_MODE = "BackgroundTracker.arg_track_mode"

class LocationTrackingService : BaseService(), RxSubscriptionsDelegate by RxSubscriptionDelegateImpl() {
    companion object {
        const val CHANNEL_ID = "GPS_CHANNEL"
    }

    private val syncDisposable = CompositeDisposable()
    var trackBeginningTime: Long? = null
    var isTrackEmpty = true

    @Inject lateinit var prefInteractor: LocationPreferenceInteractor
    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var trackInteractor: TrackInteractor
    @Inject lateinit var uploadTrackInteractor: UploadTrackInteractor
    @Inject lateinit var notificationManager: NotificationManager

    private val locationListener = LocationListener()
    private val binder = LocationServiceBinder()
    val isTracking: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    val wayPointsCounter: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)

    override fun onBind(intent: Intent?): IBinder = binder

    private fun createNotification(wayPointsCounter: Int): Notification.Builder = Notification.Builder(applicationContext, CHANNEL_ID)
        .setContentTitle(getString(R.string.title_tracking))
        .setContentText(String.format(getString(R.string.title_way_points_collected), wayPointsCounter))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setAutoCancel(false)

    inner class LocationListener : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            FlightRecorder.i { "Location Changed. lat:${location.latitude}, lon:${location.longitude}" }
            trackInteractor.saveWayPoint(location.toWayPoint(trackBeginningTime!!))
                .doOnComplete {
                    val wp = wayPointsCounter.value!!.inc()
                    wayPointsCounter.onNext(wp)
                    if (isTrackEmpty && wp > 1) {
                        isTrackEmpty = false
                    }
                    notificationManager.notify(FOREGROUND_ID_LOCATION_TRACKING, createNotification(wp).build())
                }
                .subscribeUi()
        }

        override fun onProviderDisabled(provider: String) = FlightRecorder.w { "onProviderDisabled: $provider" } /*todo: handle user's geolocation permission revoking*/
        override fun onProviderEnabled(provider: String) = FlightRecorder.w { "onProviderEnabled: $provider" }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = Unit /* Cause DEPRECATED */
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(FOREGROUND_ID_LOCATION_TRACKING, createNotification(wayPointsCounter.value!!).build())
        when (intent.action) {
            ACTION_RENAME_TRACK_AND_STOP_TRACKING -> renameTrackAndStopTracking(intent.extras?.getCharSequence(ARG_AUTO_SAVE, null))

            ACTION_START_TRACKING -> startTracking(
                intent.extras!!.getParcelable(ARG_TRACK_MODE)!!
            )

            ACTION_DISCARD_TRACK -> discardTrack()
        }
        return START_STICKY
    }

    private fun discardTrack() = trackInteractor.deleteTrack(trackBeginningTime!!)/*todo delete on server*/
        .doOnError { stopTracking() }
        .subscribeUi { stopTracking() }

    override fun onCreate() {
        appComponent.inject(this)
        super.onCreate()
    }

    /*TODO handle deleting or save unsaved track when system kills service*/
    override fun onDestroy() {
        disposeSubscriptions()
        super.onDestroy()

        /* to prevent leaks dispose all the subscriptions here (in case system kills service to free the resources)*/
        stopForeground(true)
        isTracking.onNext(false)
        syncDisposable.clear()
        kotlin.runCatching { locationManager.removeUpdates(locationListener) }
    }

    private fun stopTracking() {
        stopForeground(true)
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (ex: Exception) {
                FlightRecorder.e(label = "Failed to remove location listeners", error = ex)
            } finally {
                isTracking.onNext(false) /* ? */
                syncDisposable.clear()
                wayPointsCounter.onNext(0)
            }
        }
        stopSelf()
    }

    private fun renameTrackAndStopTracking(title: CharSequence?) {
        if (title != null) {
            trackInteractor.renameTrack(trackBeginningTime!!, title.toString())
                .subscribeUi { saveResult ->
                    when (saveResult) {
                        is SaveTrackResult.Success -> successToast(R.string.toast_track_saved) /*todo check availability of toasts from service in latest versions*/
                        is SaveTrackResult.DatabaseCorruptionError -> errorToast(R.string.error_couldnt_save_track)
                    }
                }
        }
        uploadTrackInteractor.uploadTrack(trackBeginningTime!!) /*process needed to be reflected in UI -?- */

        /** for integrity testing purposes */
        trackInteractor.getWayPointsById(trackBeginningTime!!)
            .subscribeUi { wpAmountInDb ->
                if (wayPointsCounter.value != wpAmountInDb) {
                    with("!WayPoints mismatch! actual (${wayPointsCounter.value}), database ($wpAmountInDb)") {
                        FlightRecorder.e(this, RuntimeException())
                        errorToast(this)
                    }
                }
                stopTracking()
            }
    }

    private fun startTracking(trackMode: TrackMode) {
        val timeUpdateInterval = (prefInteractor.getTimeIntervalBetweenUpdates()
            .blockingGet() as GetTimeIntervalResult.Success).timeInterval

        val distanceBetweenUpdates = (prefInteractor.getDistanceBetweenUpdates()
            .blockingGet() as GetDistanceResult.Success).distanceBetweenUpdates

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeUpdateInterval, distanceBetweenUpdates, locationListener)
            isTracking.onNext(true)
            isTrackEmpty = true
            trackBeginningTime = System.currentTimeMillis()

            trackInteractor.saveTrack(
                Track(
                    time = trackBeginningTime!!,
                    title = trackBeginningTime!!.toReadableDate(),
                    trackMode = trackMode
                )
            )
                .doOnError { FlightRecorder.e("failed to initially save track!", error = it) }
                .subscribeUi()

            syncDisposable += (
                    if (BuildConfig.DEBUG)
                        Observable.interval(15, TimeUnit.SECONDS) else Observable.interval(10, TimeUnit.MINUTES)
                    )
                .observeOn(Schedulers.newThread())
                .doOnNext { uploadTrackInteractor.uploadTrack(trackBeginningTime!!) }
                .subscribe()

        } catch (ex: SecurityException) {
            isTracking.onNext(false)
            stopSelf()
            FlightRecorder.e(label = "Failed to request location updates", error = ex)
        } catch (ex: IllegalArgumentException) {
            stopSelf()
            isTracking.onNext(false)
            FlightRecorder.e(label = "GPS provider does not exist (${ex.localizedMessage})", error = ex)
        }
    }

    /*FIXME: LeakCanary concerns about $this.
    * https://github.com/harshvardhanrastogi/leak-proof-service
    * */
    inner class LocationServiceBinder : Binder() {
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }
}