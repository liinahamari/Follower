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

package dev.liinahamari.follower.services

import android.app.Notification
import android.content.Intent
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseService
import dev.liinahamari.follower.base.FOREGROUND_ID_AUTO_TRACKING_SCHEDULING
import dev.liinahamari.follower.ext.appComponent
import dev.liinahamari.follower.helper.delegates.RxSubscriptionDelegateImpl
import dev.liinahamari.follower.helper.delegates.RxSubscriptionsDelegate
import dev.liinahamari.follower.screens.settings.AutoTrackingSchedulingUseCase
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import javax.inject.Inject

class AutoTrackingSchedulingService
    : BaseService(),
    RxSubscriptionsDelegate by RxSubscriptionDelegateImpl() {
    companion object {
        const val CHANNEL_ID = "AUTO_TRACKING_SCHEDULING_CHANNEL"
    }

    override fun onDestroy() {
        disposeSubscriptions()
        super.onDestroy()
    }

    @Inject lateinit var autoTrackingSchedulingUseCase: AutoTrackingSchedulingUseCase

    override fun onCreate() {
        appComponent.inject(this)
        super.onCreate()
        startForeground(FOREGROUND_ID_AUTO_TRACKING_SCHEDULING, createNotification().build())
    }

    private fun createNotification(): Notification.Builder = Notification.Builder(applicationContext, CHANNEL_ID)
        .setContentTitle(getString(R.string.title_scheduling))
        .setContentText("")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setAutoCancel(false)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_STICKY.also {
        super.onStartCommand(intent, flags, startId)
        autoTrackingSchedulingUseCase.setupStartAndStop()//todo think about threads
            .doOnError {
                FlightRecorder.e("Auto-tracking scheduling", it)
                stopForeground(true)
                stopSelf()
            }
            .addToDisposable {
                FlightRecorder.i { "${this.javaClass.name} scheduling successful" }
                stopForeground(true) /*todo check it's working*/
                stopSelf()
            }
    }
}