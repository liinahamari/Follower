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