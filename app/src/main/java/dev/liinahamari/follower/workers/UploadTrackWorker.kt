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

package dev.liinahamari.follower.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import dev.liinahamari.follower.di.modules.ChildWorkerFactory
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.networking.ServerService
import dev.liinahamari.follower.networking.ServerTrack
import dev.liinahamari.follower.screens.tracking_control.WORKER_EXTRA_TRACK_ID
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Provider

class UploadTrackWorker(private val trackDao: TrackDao, private val serverService: ServerService, workerParams: WorkerParameters, context: Context) : RxWorker(context, workerParams) {
    override fun createWork(): Single<Result> = trackDao.getTrackWithWayPoints(inputData.getLong(WORKER_EXTRA_TRACK_ID, -1L))
        .map {
            ServerTrack( /*to mapper*/
                time = it.track.time,
                title = it.track.title,
                wayPoints = it.wayPoints.map { wp -> wp.longitude to wp.latitude }.toTypedArray(),
                it.track.trackMode
            )
        }
        .flatMap { serverService.put(it).map { Result.success() } }
        .onErrorReturn { Result.failure() }

    class Factory @Inject constructor(private val serverService: Provider<ServerService>, private val trackDao: Provider<TrackDao>) : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker = UploadTrackWorker(trackDao.get(), serverService.get(), params, appContext)
    }
}