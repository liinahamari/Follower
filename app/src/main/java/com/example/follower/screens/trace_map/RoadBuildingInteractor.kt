package com.example.follower.screens.trace_map

import android.content.Context
import com.example.follower.R
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.model.PersistedTrackResult
import com.example.follower.model.PreferencesRepository
import com.example.follower.model.TrackDao
import io.reactivex.Single
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint

@RoadBuildingScope
class RoadBuildingInteractor constructor(private val context: Context, private val trackDao: TrackDao, private val baseComposers: BaseComposers, private val prefRepo: PreferencesRepository, private val osmRoadManager: OSRMRoadManager){
    fun getRoad(trackId: Long): Single<GetRoadResult> = prefRepo.getPersistedTrackRepresentation()
        .flatMap { lineOrMarkerSet ->
            if (lineOrMarkerSet is PersistedTrackResult.Success) {
                when (lineOrMarkerSet.value) {
                    context.getString(R.string.pref_line) -> {
                        trackDao.getTrackWithWayPoints(trackId)
                            .flattenAsObservable { it.wayPoints }
                            .map { GeoPoint(it.latitude, it.longitude, 0.0) }
                            .toList()
                            .map { osmRoadManager.getRoad(ArrayList(it)) }
                            .map<GetRoadResult> { GetRoadResult.SuccessfulLine(it) }
                            .onErrorReturn { GetRoadResult.SharedPrefsError }
                    }
                    context.getString(R.string.pref_marker_set) -> {
                        trackDao.getTrackWithWayPoints(trackId)
                            .map { it.wayPoints.map { wayPoint -> Pair(wayPoint.longitude, wayPoint.latitude) } }
                            .map<GetRoadResult> { GetRoadResult.SuccessfulMarkerSet(it) }
                            .onErrorReturn { GetRoadResult.DatabaseCorruptionError }
                    }
                    else -> throw IllegalStateException()
                }
            } else return@flatMap Single.just(GetRoadResult.SharedPrefsError)
        }
        .doOnError { it.printStackTrace() } /*FIXME : wtf it isn't working in composers?*/
        .compose(baseComposers.applySingleSchedulers())
}

sealed class GetRoadResult {
    data class SuccessfulLine(val road: Road) : GetRoadResult()
    data class SuccessfulMarkerSet(val markerSet: List<Pair<Longitude, Latitude>>) : GetRoadResult()
    object DatabaseCorruptionError : GetRoadResult()
    object SharedPrefsError : GetRoadResult()
}
