package dev.liinahamari.follower.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.screens.settings.AutoTrackingSchedulingUseCase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

private const val ID = 213152

class AutoTrackingSchedulingService : Service() {
    companion object {
        const val CHANNEL_ID = "AUTO_TRACKING_SCHEDULING_CHANNEL"
    }

    @Inject lateinit var autoTrackingSchedulingUseCase: AutoTrackingSchedulingUseCase
    @Inject lateinit var logger: FlightRecorder
    private val disposable = CompositeDisposable()

    override fun onCreate() {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate()
        startForeground(ID, createNotification().build())
    }

    private fun createNotification(): Notification.Builder = Notification.Builder(applicationContext, CHANNEL_ID)
        .setContentText(getString(R.string.title_scheduling))
        .setAutoCancel(false)

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        disposable += autoTrackingSchedulingUseCase.setupStartAndStop().subscribe({
            logger.i { "${this.javaClass.name} scheduling successful" }
            stopForeground(true) /*todo check it's working*/
        }, {
            logger.e("Auto-tracking scheduling", it)
            stopForeground(true)
        })
        return START_STICKY
    }
}