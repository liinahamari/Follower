package dev.liinahamari.follower.services

import android.app.Notification
import android.content.Intent
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseService
import dev.liinahamari.follower.base.FOREGROUND_ID_AUTO_TRACKING_SCHEDULING
import dev.liinahamari.follower.screens.settings.AutoTrackingSchedulingUseCase
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class AutoTrackingSchedulingService : BaseService() {
    companion object {
        const val CHANNEL_ID = "AUTO_TRACKING_SCHEDULING_CHANNEL"
    }

    @Inject lateinit var autoTrackingSchedulingUseCase: AutoTrackingSchedulingUseCase

    override fun onCreate() {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate()
        startForeground(FOREGROUND_ID_AUTO_TRACKING_SCHEDULING, createNotification().build())
    }

    private fun createNotification(): Notification.Builder = Notification.Builder(applicationContext, CHANNEL_ID)
        .setContentText(getString(R.string.title_scheduling))
        .setAutoCancel(false)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        subscriptions += autoTrackingSchedulingUseCase.setupStartAndStop().subscribe({
            logger.i { "${this.javaClass.name} scheduling successful" }
            stopForeground(true) /*todo check it's working*/
        }, {
            logger.e("Auto-tracking scheduling", it)
            stopForeground(true)
        })
        return START_STICKY
    }
}