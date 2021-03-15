package com.example.follower.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.example.follower.di.modules.ChildWorkerFactory
import com.example.follower.model.TrackDao
import com.example.follower.networking.ServerService
import com.example.follower.networking.ServerTrack
import com.example.follower.screens.tracking_control.WORKER_EXTRA_TRACK_ID
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Provider

class UploadTrackWorker(private val trackDao: TrackDao, private val serverService: ServerService, workerParams: WorkerParameters, context: Context) : RxWorker(context, workerParams) {
    override fun createWork(): Single<Result> = trackDao.getTrackWithWayPoints(inputData.getLong(WORKER_EXTRA_TRACK_ID, -1L))
        .map {
            ServerTrack( /*to mapper*/
                time = it.track.time,
                title = it.track.title,
                wayPoints = it.wayPoints.map { wp -> wp.longitude to wp.latitude }.toTypedArray()
            )
        }
        .flatMap { serverService.replace(it, it.time).map { Result.success() } }
        .onErrorReturn { Result.failure() }

    class Factory @Inject constructor(private val serverService: Provider<ServerService>, private val trackDao: Provider<TrackDao>) : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker = UploadTrackWorker(trackDao.get(), serverService.get(), params, appContext)
    }
}