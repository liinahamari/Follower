package dev.liinahamari.follower.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dev.liinahamari.follower.di.modules.ChildWorkerFactory
import dev.liinahamari.follower.services.location_tracking.ACTION_START_TRACKING
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import javax.inject.Inject

const val TAG_AUTO_START_WORKER = "worker_auto_start"

class AutoStartTrackingWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d(TAG_AUTO_START_WORKER, "start auto")
        context.applicationContext.startService(
            Intent(context.applicationContext, LocationTrackingService::class.java)
            .apply {
                action = ACTION_START_TRACKING
            })
        return Result.success()
    }

    class Factory @Inject constructor() : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker = AutoStartTrackingWorker(appContext, params)
    }
}