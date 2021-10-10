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

package dev.liinahamari.follower.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.annotation.VisibleForTesting
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.activityImplicitLaunch
import dev.liinahamari.follower.model.PermissionToNotifyAboutLowBatteryResult
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.screens.low_battery.LowBatteryNotifierActivity
import dev.liinahamari.follower.services.LowBatteryNotificationService
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import javax.inject.Inject
import javax.inject.Named

@VisibleForTesting const val BATTERY_THRESHOLD_PERCENTAGE = 23

class BatteryStateHandlingUseCase @Inject constructor(
    @Named(APP_CONTEXT) private val context: Context,
    @JvmField val batteryManager: BatteryManager? = null,
    private val preferencesRepository: PreferencesRepository
) {
    @SuppressLint("NewApi")
    fun execute() {
        preferencesRepository.isForbiddenToNotifyLowBatteryAtNight()
            .filter { it is PermissionToNotifyAboutLowBatteryResult.Success && it.permitted }
            /** blockingGet() for Maybe.empty() which is result of filtering by permission returns null*/
            .blockingGet()
            ?.apply {
                (batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
                    context.registerReceiver(null, intentFilter)?.let { intent ->
                        val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        level * 100 / scale.toFloat()
                    }
                }?.toInt())?.also {
                    FlightRecorder.i { "Battery level is $it" }
                    if (it < BATTERY_THRESHOLD_PERCENTAGE) {
                        context.activityImplicitLaunch(LowBatteryNotificationService::class.java, LowBatteryNotifierActivity::class.java)
                    }
                }
            }
    }
}