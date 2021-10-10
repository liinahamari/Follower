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

package dev.liinahamari.follower.base

import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.toColorfulString
import dev.liinahamari.follower.helper.Notifier
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val DEFAULT_ALARM_PLAYING_TIME = 3L

abstract class ForegroundService : BaseService() {
    companion object {
        const val ACTION_TERMINATE = "ForegroundService.ACTION_TERMINATE"
        const val ACTION_STOP_FOREGROUND = "ForegroundService.STOP_FOREGROUND"
        const val ACTION_SHOW_NOTIFICATION = "ForegroundService.SHOW_NOTIFICATION"
    }

    @Inject lateinit var notifier: Notifier
    protected val timer: Observable<Long> = Observable.timer(DEFAULT_ALARM_PLAYING_TIME, TimeUnit.MINUTES, AndroidSchedulers.mainThread())

    abstract fun getActionsRequestCode(): Int
    abstract fun getActivity(): Class<out Activity>
    abstract fun getTitle(intent: Intent?): String
    abstract fun getServiceId(): Int
    @DrawableRes
    abstract fun getIcon(): Int

    override fun onCreate() {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate()
        notifier.start()
    }

    protected fun getCancelAction(): NotificationCompat.Action = NotificationCompat.Action(
        0,
        getString(R.string.dismiss).toColorfulString(Color.RED),
        PendingIntent.getService(
            this,
            getActionsRequestCode(),
            Intent(this, this::class.java)
                .apply { this.action = ACTION_TERMINATE },
            FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    )

    protected fun getFullscreenIntent(bundle: Bundle, activity: Class<out Activity>): PendingIntent = PendingIntent.getActivity(
        this,
        getActionsRequestCode(),
        Intent(this, activity)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtras(bundle)
            },
        FLAG_IMMUTABLE
    )

    override fun onDestroy() {
        super.onDestroy()
        notifier.stop()
    }
}

