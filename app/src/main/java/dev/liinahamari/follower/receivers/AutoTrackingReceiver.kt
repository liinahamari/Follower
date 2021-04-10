package dev.liinahamari.follower.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.liinahamari.follower.services.AutoTrackingSchedulingService
import dev.liinahamari.follower.services.location_tracking.ACTION_START_TRACKING
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import java.lang.IllegalStateException

class AutoTrackingReceiver : BroadcastReceiver() {
    enum class ActionMode {
        ACTION_MODE_START, ACTION_MODE_STOP
    }

    companion object {
        fun createIntent(actionMode: ActionMode, context: Context): Intent = Intent(context, AutoTrackingReceiver::class.java).apply {
            action = actionMode.name
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ActionMode.ACTION_MODE_START.name -> {
                context.applicationContext.startService(Intent(context.applicationContext, LocationTrackingService::class.java).apply {
                    action = ACTION_START_TRACKING
                })
            }
            ActionMode.ACTION_MODE_STOP.name -> context.applicationContext.stopService(Intent(context.applicationContext, LocationTrackingService::class.java))
            else -> throw IllegalStateException()
        }
        context.applicationContext.startForegroundService(Intent(context.applicationContext, AutoTrackingSchedulingService::class.java))
    }
}