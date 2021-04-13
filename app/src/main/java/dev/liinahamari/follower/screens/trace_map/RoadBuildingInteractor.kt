package dev.liinahamari.follower.screens.trace_map

import android.content.Context
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.toReadableDate
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.model.PreferenceRepository
import dev.liinahamari.follower.model.TrackDao
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import javax.inject.Named

@ExperimentalCoroutinesApi
@RoadBuildingScope
class RoadBuildingInteractor constructor(
    @Named(APP_CONTEXT) private val context: Context,
    private val trackDao: TrackDao,
    private val prefRepo: PreferenceRepository,
    private val osmRoadManager: OSRMRoadManager
) {
    fun getRoad(trackId: Long): Flowable<GetRoadResult> = prefRepo.trackRepresentation
        .flatMapSingle { lineOrMarkerSet ->
            when (lineOrMarkerSet) {
                context.getString(R.string.pref_line) -> {
                    trackDao.getTrackWithWayPoints(trackId)
                        .flattenAsObservable { it.wayPoints }
                        .toList()
                        .map<GetRoadResult> {
                            val geoPoints = ArrayList(it.map { wp -> GeoPoint(wp.latitude, wp.longitude) })
                            GetRoadResult.SuccessfulLine(
                                TrackUi.Road(
                                    road = RoadManager.buildRoadOverlay(osmRoadManager.getRoad(geoPoints)),
                                    startPoint = WayPointUi(it.first().latitude, it.first().longitude, it.first().time.toReadableDate()),
                                    finishPoint = WayPointUi(it.last().latitude, it.last().longitude, it.last().time.toReadableDate()),
                                    boundingBox = BoundingBox.fromGeoPointsSafe(geoPoints)
                                )
                            )
                        }
                        .onErrorReturn { GetRoadResult.SharedPrefsError }
                }
                context.getString(R.string.pref_marker_set) -> {
                    trackDao.getTrackWithWayPoints(trackId)
                        .map { it.wayPoints.map { wayPoint -> WayPointUi(wayPoint.latitude, wayPoint.longitude, wayPoint.time.toReadableDate()) } }
                        .map<GetRoadResult> {
                            GetRoadResult.SuccessfulMarkerSet(
                                TrackUi.Markers(
                                    wayPoints = it.drop(1).dropLast(1),
                                    startPoint = it.first(),
                                    finishPoint = it.last(),
                                    boundingBox = BoundingBox.fromGeoPointsSafe(it.map { wp -> GeoPoint(wp.lat, wp.lon) })
                                )
                            )
                        }
                        .onErrorReturn { GetRoadResult.DatabaseCorruptionError }
                }
                else -> throw IllegalStateException()
            }
        }
}

sealed class GetRoadResult {
    data class SuccessfulLine(val road: TrackUi.Road) : GetRoadResult()
    data class SuccessfulMarkerSet(val markerSet: TrackUi.Markers) : GetRoadResult()
    object DatabaseCorruptionError : GetRoadResult()
    object SharedPrefsError : GetRoadResult()
}
