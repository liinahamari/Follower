package com.example.follower.interactors

import com.example.follower.db.Track
import com.example.follower.db.TrackDao
import com.example.follower.db.WayPointDao
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.screens.map.WayPoint
import io.reactivex.Single
import javax.inject.Inject

class TrackInteractor @Inject constructor(private val trackDao: TrackDao, private val wayPointDao: WayPointDao, private val logger: FlightRecorder, private val baseComposers: BaseComposers) {
    fun saveTrack(track: Track, wayPoints: List<WayPoint>): Single<SaveTrackResult> = trackDao.insert(track)
        .doOnSuccess { trackId -> wayPoints.forEach { it.trackId = trackId } }
        .flatMapCompletable { wayPointDao.insertAll(wayPoints) }
        .toSingleDefault<SaveTrackResult>(SaveTrackResult.Success)
        .onErrorReturn { SaveTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())
        .doOnSuccess { logger.i { "Track saved with ${wayPoints.size} wayPoints" } }

    fun getTrackById(taskId: Long): Single<GetTrackResult> = trackDao.getTrackWithWayPoints(taskId)
        .map<GetTrackResult> { GetTrackResult.Success(it.wayPoints) }
        .onErrorReturn { GetTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())
}

sealed class SaveTrackResult {
    object Success : SaveTrackResult()
    object DatabaseCorruptionError : SaveTrackResult()
}

sealed class GetTrackResult {
    data class Success(val waypoints: List<WayPoint>) : GetTrackResult()
    object DatabaseCorruptionError : GetTrackResult()
}