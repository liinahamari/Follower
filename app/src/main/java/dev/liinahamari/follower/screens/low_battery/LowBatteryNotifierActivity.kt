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

package dev.liinahamari.follower.screens.low_battery

import android.content.Intent
import android.os.Bundle
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.DEFAULT_ALARM_PLAYING_TIME
import dev.liinahamari.follower.base.ForegroundService
import dev.liinahamari.follower.base.NotifyingActivity
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.services.LowBatteryNotificationService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.synthetic.main.activity_low_battery_notifier.*
import java.util.concurrent.TimeUnit

class LowBatteryNotifierActivity : NotifyingActivity(R.layout.activity_low_battery_notifier) {
    private val subscriptions = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        shutdownNotifiers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscriptions += Observable.timer(DEFAULT_ALARM_PLAYING_TIME, TimeUnit.MINUTES, AndroidSchedulers.mainThread())
            .subscribe { shutdownNotifiers() }

        subscriptions += buttonGotIt.clicks()
            .throttleFirst()
            .subscribe { shutdownNotifiers() }
    }

    private fun shutdownNotifiers() {
        startService(Intent(this@LowBatteryNotifierActivity, LowBatteryNotificationService::class.java).apply {
            action = ForegroundService.ACTION_TERMINATE
        })
        finish()
    }
}