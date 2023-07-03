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

package dev.liinahamari.follower.screens.trace_map

import android.content.Context
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.toReadableDate
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.model.PersistedTrackResult
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.model.getTrackColor
import dev.liinahamari.follower.model.toMean
import io.reactivex.rxjava3.core.Single
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import javax.inject.Named

@RoadBuildingScope
class RoadBuildingInteractor constructor(
    @Named(APP_CONTEXT) private val context: Context, private val trackDao: TrackDao, private val baseComposers: BaseComposers, private val prefRepo: PreferencesRepository, private val osmRoadManager: OSRMRoadManager
) {
    fun getAllRoads(): Single<GetAllRoadsResult> = trackDao.getAllTracksWithWayPoints().map {
            var totalLength = .0
            val allGeoPoints = mutableListOf<GeoPoint>()
            val listOfLines: List<Polyline> = it.map {
                val geoPoints = ArrayList(it.wayPoints.map { wp -> GeoPoint(wp.latitude, wp.longitude) }).also { allGeoPoints.addAll(it) }
                val road = osmRoadManager.apply { setMean(it.track.trackMode.toMean()) }.getRoad(geoPoints)
                totalLength += road.mLength

                RoadManager.buildRoadOverlay(
                    road, it.track.trackMode.getTrackColor(), 5f
                )
            }
            GetAllRoadsResult.SuccessfulLine(
                TracksUi(roads = listOfLines, length = totalLength, boundingBox = BoundingBox.fromGeoPointsSafe(allGeoPoints))
            )
        }.compose(baseComposers.applySingleSchedulers())

    fun updateLength(length: Double, trackId: Long): Single<UpdateTrackLengthResult> = trackDao.findByTrackId(trackId).flatMapCompletable {
        trackDao.update(it.copy(length = length))
    }.toSingleDefault<UpdateTrackLengthResult>(UpdateTrackLengthResult.Success)
        .onErrorReturn { UpdateTrackLengthResult.Error }

    fun getRoad(trackId: Long): Single<GetRoadResult> = prefRepo.getPersistedTrackRepresentation().flatMap { lineOrMarkerSet ->
            if (lineOrMarkerSet is PersistedTrackResult.Success) {
                when (lineOrMarkerSet.value) {
                    context.getString(R.string.pref_line) -> {
                        trackDao.getTrackWithWayPoints(trackId).map<GetRoadResult> {
                                val geoPoints = ArrayList(it.wayPoints.map { wp -> GeoPoint(wp.latitude, wp.longitude) })
                                val road = osmRoadManager.apply { setMean(it.track.trackMode.toMean()) }.getRoad(geoPoints)
                                GetRoadResult.SuccessfulLine(
                                    TrackUi.Road(
                                        trackId = trackId, road = RoadManager.buildRoadOverlay(
                                            road, it.track.trackMode.getTrackColor(), 5f
                                        ), length = road.mLength, startPoint = WayPointUi(it.wayPoints.first().latitude, it.wayPoints.first().longitude, it.wayPoints.first().time.toReadableDate()), finishPoint = WayPointUi(it.wayPoints.last().latitude, it.wayPoints.last().longitude, it.wayPoints.last().time.toReadableDate()), boundingBox = BoundingBox.fromGeoPointsSafe(geoPoints)
                                    )
                                )
                            }.onErrorReturn { GetRoadResult.SharedPrefsError }
                    }

                    context.getString(R.string.pref_marker_set) -> {
                        trackDao.getTrackWithWayPoints(trackId).map { it.wayPoints.map { wayPoint -> WayPointUi(wayPoint.latitude, wayPoint.longitude, wayPoint.time.toReadableDate()) } }.map<GetRoadResult> {
                                GetRoadResult.SuccessfulMarkerSet(
                                    TrackUi.Markers(
                                        wayPoints = it.drop(1).dropLast(1), startPoint = it.first(), finishPoint = it.last(), boundingBox = BoundingBox.fromGeoPointsSafe(it.map { wp -> GeoPoint(wp.lat, wp.lon) })
                                    )
                                )
                            }.onErrorReturn { GetRoadResult.DatabaseCorruptionError }
                    }

                    else -> throw IllegalStateException()
                }
            } else return@flatMap Single.just(GetRoadResult.SharedPrefsError)
        }.compose(baseComposers.applySingleSchedulers())
}

sealed class GetRoadResult {
    data class SuccessfulLine(val road: TrackUi.Road) : GetRoadResult()
    data class SuccessfulMarkerSet(val markerSet: TrackUi.Markers) : GetRoadResult()
    object DatabaseCorruptionError : GetRoadResult()
    object SharedPrefsError : GetRoadResult()
}

sealed class GetAllRoadsResult {
    data class SuccessfulLine(val roads: TracksUi) : GetAllRoadsResult()
    object DatabaseCorruptionError : GetAllRoadsResult()
    object SharedPrefsError : GetAllRoadsResult()
}

sealed class UpdateTrackLengthResult {
    object Success : UpdateTrackLengthResult()
    object Error : UpdateTrackLengthResult()
}
