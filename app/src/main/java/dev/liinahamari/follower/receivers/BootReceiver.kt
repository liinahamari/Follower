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

package dev.liinahamari.follower.receivers

import android.app.AlarmManager
import android.app.AlarmManager.INTERVAL_HOUR
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import android.content.SharedPreferences
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.getBooleanOf
import dev.liinahamari.follower.ext.minutesToMilliseconds
import dev.liinahamari.follower.ext.scheduleLowBatteryChecker
import dev.liinahamari.follower.services.AutoTrackingSchedulingService
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import javax.inject.Inject

const val BATTERY_CHECKER_ID = 101
private const val BOOT_RECEIVED_LOG = "Boot completed received in Follower application."

class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var alarmManager: AlarmManager

    /**
     * To test "boot completed" event is handling you'll need a rooted device and run in terminal:
     *  adb root
     *  adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p dev.liinahamari.follower
     *
     * In case {@link #onReceive(Context, Intent)} succeeds receiving, you'll see {@link #BOOT_RECEIVED_LOG}
     * */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_BOOT_COMPLETED) {

            FlightRecorder.i(toPrintInLogcat = true) {
                BOOT_RECEIVED_LOG
            }

            (context.applicationContext as FollowerApp).appComponent.inject(this)

            with(context) {
                if (sharedPreferences.getBooleanOf(getString(R.string.pref_enable_auto_tracking))) {
                    startForegroundService(Intent(applicationContext, AutoTrackingSchedulingService::class.java))
                }
                scheduleLowBatteryChecker()
            }
        }
    }
}