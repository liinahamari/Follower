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

package dev.liinahamari.follower.ext

import android.app.*
import android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES
import android.app.AlarmManager.INTERVAL_HOUR
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.base.ForegroundService.Companion.ACTION_SHOW_NOTIFICATION
import dev.liinahamari.follower.base.ForegroundService.Companion.ACTION_STOP_FOREGROUND
import dev.liinahamari.follower.receivers.BATTERY_CHECKER_ID
import dev.liinahamari.follower.receivers.LowBatteryReceiver

@Suppress(
    "DEPRECATION"
    /** """this method is no longer available to third party applications""" -- but we do care only about tracking our application's services*/
)
fun Context.isServiceRunning(serviceClass: Class<*>) = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getRunningServices(Int.MAX_VALUE).any { serviceClass.name == it.service.className }

fun FragmentActivity.openAppSettings() = startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${BuildConfig.APPLICATION_ID}")))

fun Fragment.startForegroundService(serviceClass: Class<out Service>, bundle: Bundle? = null, action: String? = null) =
    requireActivity().applicationContext.startForegroundService(Intent(requireActivity().applicationContext, serviceClass)
        .apply {
            bundle?.let { putExtras(it) }
            action?.let { this.action = action }
        })

fun Fragment.stopService(serviceClass: Class<out Service>) = requireActivity().application.stopService(Intent(requireActivity().applicationContext, serviceClass))

fun Context.isIgnoringBatteryOptimizations() = (getSystemService(Context.POWER_SERVICE) as PowerManager?)?.isIgnoringBatteryOptimizations(packageName) == true
fun Fragment.isIgnoringBatteryOptimizations() = (requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager?)?.isIgnoringBatteryOptimizations(requireActivity().packageName) == true
fun Activity.isIgnoringBatteryOptimizations() = (getSystemService(Context.POWER_SERVICE) as PowerManager?)?.isIgnoringBatteryOptimizations(packageName) == true

/** workaround for Android 10 restrictions to launch activities in background:
 *  https://developer.android.com/guide/components/activities/background-starts
 * */
fun Context.activityImplicitLaunch(service: Class<out Service>, activity: Class<out Activity>, bundle: Bundle? = null) {
    if (Build.VERSION.SDK_INT >= 29 && (applicationContext as FollowerApp).isAppInForeground.not()) {
        ContextCompat.startForegroundService(this, Intent(this, service).apply {
            action = ACTION_SHOW_NOTIFICATION
            bundle?.let { putExtras(it) }
        })
    } else {
        startService(Intent(this, service).apply {
            bundle?.let { putExtras(it) }
            action = ACTION_STOP_FOREGROUND
        })
        startActivity(Intent(this, activity).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            bundle?.let { putExtras(it) }
        })
    }
}

/** Runs once-an-hour checker of battery state */
fun Context.scheduleLowBatteryChecker(initialDelayInMinutes: Long = 3L) = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setRepeating(
    AlarmManager.RTC_WAKEUP,
    System.currentTimeMillis() + minutesToMilliseconds(initialDelayInMinutes),
    if (BuildConfig.DEBUG) INTERVAL_FIFTEEN_MINUTES else INTERVAL_HOUR,
    PendingIntent.getBroadcast(
        this,
        BATTERY_CHECKER_ID,
        Intent(this, LowBatteryReceiver::class.java),
        FLAG_IMMUTABLE
    )
)

fun Context.cancelLowBatteryChecker() = (this.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
    .cancel(PendingIntent.getBroadcast(this, BATTERY_CHECKER_ID, Intent(this, LowBatteryReceiver::class.java), FLAG_MUTABLE))

@Suppress("DEPRECATION")
fun Context.getVersionCode(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()
} else {
    packageManager.getPackageInfo(packageName, 0).versionCode.toString()
}
