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
                wayPoints = it.wayPoints.map { wp -> wp.longitude to wp.latitude }.toTypedArray()
            )
        }
        .flatMap { serverService.put(it).map { Result.success() } }
        .onErrorReturn { Result.failure() }

    class Factory @Inject constructor(private val serverService: Provider<ServerService>, private val trackDao: Provider<TrackDao>) : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker = UploadTrackWorker(trackDao.get(), serverService.get(), params, appContext)
    }
}