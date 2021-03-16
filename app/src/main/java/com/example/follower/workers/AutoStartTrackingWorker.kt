package com.example.follower.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.follower.services.ACTION_START_TRACKING
import com.example.follower.services.LocationTrackingService

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
}