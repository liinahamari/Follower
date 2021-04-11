package dev.liinahamari.follower.ext

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.liinahamari.follower.BuildConfig

@Suppress(
    "DEPRECATION"
    /** """this method is no longer available to third party applications""" -- but we do care only about tracking our application's services*/
)
fun Context.isServiceRunning(serviceClass: Class<*>) = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getRunningServices(Int.MAX_VALUE).any { serviceClass.name == it.service.className }

fun FragmentActivity.openAppSettings() = startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${BuildConfig.APPLICATION_ID}")))

fun Fragment.startForegroundService(serviceClass: Class<out Service>, bundle: Bundle? = null, action: String? = null) = requireActivity().applicationContext.startForegroundService(Intent(requireActivity().applicationContext, serviceClass).apply {
    bundle?.let { putExtras(it) }
    action?.let { this.action = action }
})

fun Fragment.stopService(serviceClass: Class<out Service>) = requireActivity().application.stopService(Intent(requireActivity().applicationContext, serviceClass))

fun Context.isIgnoringBatteryOptimizations() = (getSystemService(Context.POWER_SERVICE) as PowerManager?)?.isIgnoringBatteryOptimizations(packageName) == true
fun Fragment.isIgnoringBatteryOptimizations() = (requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager?)?.isIgnoringBatteryOptimizations(requireActivity().packageName) == true
fun Activity.isIgnoringBatteryOptimizations() = (getSystemService(Context.POWER_SERVICE) as PowerManager?)?.isIgnoringBatteryOptimizations(packageName) == true
