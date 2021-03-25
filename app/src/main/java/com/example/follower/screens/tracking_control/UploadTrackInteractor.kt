package com.example.follower.screens.tracking_control

import androidx.work.*
import com.example.follower.workers.UploadTrackWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val WORKER_EXTRA_TRACK_ID = "worker_extra_track_id"

class UploadTrackInteractor @Inject constructor(private val workManager: WorkManager) {
    fun uploadTrack(trackId: Long) = workManager.enqueue(constraints<UploadTrackWorker>(trackId).build())

    private inline fun <reified T : ListenableWorker> constraints(trackId: Long) = OneTimeWorkRequestBuilder<T>()
        .setInputData(workDataOf(WORKER_EXTRA_TRACK_ID to trackId))
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .setRequiresStorageNotLow(false)
                .build()
        )
        .setBackoffCriteria(BackoffPolicy.LINEAR, 1000 * 60 * 10, TimeUnit.MILLISECONDS)
}