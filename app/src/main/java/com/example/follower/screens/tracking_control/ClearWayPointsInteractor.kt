package com.example.follower.screens.tracking_control

import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.interactors.ClearWayPointsResult
import com.example.follower.model.WayPointDao
import io.reactivex.Single
import javax.inject.Inject

@TrackingControlScope
class ClearWayPointsInteractor constructor(private val wayPointDao: WayPointDao, private val logger: FlightRecorder, private val baseComposers: BaseComposers) {
    fun clearWayPoints(trackId: Long): Single<ClearWayPointsResult> = wayPointDao.delete(trackId)
        .toSingleDefault<ClearWayPointsResult> (ClearWayPointsResult.Success)
        .onErrorReturn { ClearWayPointsResult.DatabaseCorruptionError }
        .doOnError { logger.e("wayPoints deleting", stackTrace = it.stackTrace) }
        .compose(baseComposers.applySingleSchedulers())
}