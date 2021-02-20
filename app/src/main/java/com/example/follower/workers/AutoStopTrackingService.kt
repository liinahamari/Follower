package com.example.follower.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.follower.services.ACTION_STOP_TRACKING
import com.example.follower.services.LocationTrackingService

const val TAG_AUTO_STOP_WORKER = "worker_auto_stop"

class AutoStopTrackingWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("a", "abcabcabc stop auto")

        context.applicationContext.startService(
            Intent(context.applicationContext, LocationTrackingService::class.java)
            .apply {
                action = ACTION_STOP_TRACKING
            })
        return Result.success()
    }
}