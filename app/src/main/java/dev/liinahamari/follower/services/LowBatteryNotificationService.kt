/*
 * Copyright 2020-2021 liinahamari
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.liinahamari.follower.services

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import dev.liinahamari.follower.CHANNEL_BATTERY_LOW_ID
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.ForegroundService
import dev.liinahamari.follower.helper.delegates.RxSubscriptionDelegateImpl
import dev.liinahamari.follower.helper.delegates.RxSubscriptionsDelegate
import dev.liinahamari.follower.screens.low_battery.LowBatteryNotifierActivity

const val ID_LOW_BATTERY_NOTIFICATIONS_SERVICE = 1001

class LowBatteryNotificationService : ForegroundService(), RxSubscriptionsDelegate by RxSubscriptionDelegateImpl() {
    override fun getIcon(): Int = R.drawable.ic_baseline_battery_alert_24
    override fun getActivity(): Class<out Activity> = LowBatteryNotifierActivity::class.java
    override fun getTitle(intent: Intent?): String = getString(R.string.title_battery_low)
    override fun getServiceId(): Int = ID_LOW_BATTERY_NOTIFICATIONS_SERVICE
    override fun getActionsRequestCode(): Int = 100500

    override fun onDestroy() {
        disposeSubscriptions()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent.action) {
            ACTION_SHOW_NOTIFICATION -> {
                timer.addToDisposable {
                    stopSelf()
                }

                val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_BATTERY_LOW_ID)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(getIcon())
                    .setContentText(getTitle(intent))
                    .addAction(getCancelAction())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(getFullscreenIntent(intent.extras!!, getActivity()), true)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, getIcon()))
                startForeground(getServiceId(), notificationBuilder.build())
            }
            ACTION_TERMINATE -> stopSelf()
            ACTION_STOP_FOREGROUND -> stopForeground(true)
            else -> throw IllegalStateException()
        }
        return START_NOT_STICKY
    }
}
