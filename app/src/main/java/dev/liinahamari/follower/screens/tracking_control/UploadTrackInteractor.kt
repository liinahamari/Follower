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

package dev.liinahamari.follower.screens.tracking_control

import androidx.work.*
import dev.liinahamari.follower.workers.UploadTrackWorker
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