package dev.liinahamari.follower.screens.tracking_control

import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.interactors.ClearWayPointsResult
import dev.liinahamari.follower.model.WayPointDao
import io.reactivex.Single

@TrackingControlScope
class ClearWayPointsInteractor constructor(private val wayPointDao: WayPointDao, private val logger: FlightRecorder, private val baseComposers: BaseComposers) {
    fun clearWayPoints(trackId: Long): Single<ClearWayPointsResult> = wayPointDao.delete(trackId)
        .toSingleDefault<ClearWayPointsResult> (ClearWayPointsResult.Success)
        .onErrorReturn { ClearWayPointsResult.DatabaseCorruptionError }
        .doOnError { logger.e("wayPoints deleting", stackTrace = it.stackTrace) }
        .compose(baseComposers.applySingleSchedulers())
}