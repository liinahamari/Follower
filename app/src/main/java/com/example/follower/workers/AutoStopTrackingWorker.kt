package com.example.follower.workers

import android.content.Context
import android.content.Intent
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.follower.di.modules.ChildWorkerFactory
import com.example.follower.services.location_tracking.ACTION_RENAME_TRACK_AND_STOP_TRACKING
import com.example.follower.services.location_tracking.LocationTrackingService
import javax.inject.Inject

const val TAG_AUTO_STOP_WORKER = "worker_auto_stop"

class AutoStopTrackingWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        context.applicationContext.startService(
            Intent(context.applicationContext, LocationTrackingService::class.java)
            .apply {
                action = ACTION_RENAME_TRACK_AND_STOP_TRACKING
            })
        return Result.success()
    }

    class Factory @Inject constructor() : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker = AutoStopTrackingWorker(appContext, params)
    }
}