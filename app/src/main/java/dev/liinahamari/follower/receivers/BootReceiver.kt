package dev.liinahamari.follower.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.R
import dev.liinahamari.follower.ext.getBooleanOf
import dev.liinahamari.follower.services.AutoTrackingSchedulingService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBooleanOf(context.getString(R.string.pref_enable_auto_tracking)) && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startForegroundService(Intent(context.applicationContext, AutoTrackingSchedulingService::class.java))
        }
    }
}