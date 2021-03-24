package dev.liinahamari.follower.workers

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import dev.liinahamari.follower.services.ACTION_STOP_TRACKING
import dev.liinahamari.follower.services.LocationTrackingService

const val TAG_AUTO_STOP_WORKER = "worker_auto_stop"

class AutoStopTrackingWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        context.applicationContext.startService(
            Intent(context.applicationContext, LocationTrackingService::class.java)
            .apply {
                /*TODO save!*/
                action = ACTION_STOP_TRACKING
            })
        return Result.success()
    }
}