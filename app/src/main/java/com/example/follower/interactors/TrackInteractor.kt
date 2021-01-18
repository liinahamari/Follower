package com.example.follower.interactors

import android.content.Context
import android.util.Log
import com.example.follower.R
import com.example.follower.db.entities.Track
import com.example.follower.db.entities.TrackWithWayPoints
import com.example.follower.db.entities.WayPoint
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.model.PersistedTrackResult
import com.example.follower.model.PreferencesRepository
import com.example.follower.model.TrackDao
import com.example.follower.model.WayPointDao
import com.example.follower.screens.map.Latitude
import com.example.follower.screens.map.Longitude
import io.reactivex.Single
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class TrackInteractor @Inject constructor(
    private val context: Context,
    private val trackDao: TrackDao,
    private val wayPointDao: WayPointDao,
    private val logger: FlightRecorder,
    private val baseComposers: BaseComposers,
    private val prefRepo: PreferencesRepository
) {

    fun saveTrack(track: Track, wayPoints: List<WayPoint>): Single<SaveTrackResult> = trackDao.insert(track)
        .doOnSuccess { trackId -> wayPoints.forEach { it.trackId = trackId } }
        .flatMapCompletable { wayPointDao.insertAll(wayPoints) }
        .toSingleDefault<SaveTrackResult>(SaveTrackResult.Success)
        .onErrorResumeNext {
            trackDao.delete(track.time)
                .andThen { logger.wtf { "Can't save Track..." } }
                .toSingleDefault(SaveTrackResult.DatabaseCorruptionError)
        }
        .onErrorReturn { SaveTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())
        .doOnSuccess { logger.i { "Track saved with ${wayPoints.size} wayPoints" } }

    fun getTrackById(taskId: Long): Single<GetTrackResult> = prefRepo.getPersistedTrackRepresentation()
        .flatMap { lineOrMarkerSet ->
            if (lineOrMarkerSet is PersistedTrackResult.Success) {
                when (lineOrMarkerSet.value) {
                    context.getString(R.string.pref_line) -> {
                        trackDao.getTrackWithWayPoints(taskId)
                            .flattenAsObservable { it.wayPoints }
                            .map { GeoPoint(it.latitude, it.longitude, 0.0) }
                            .toList()
                            .map { OSRMRoadManager(context).getRoad(ArrayList(it)) }
                            .map<GetTrackResult> { GetTrackResult.SuccessLine(it) }
                            .onErrorReturn { GetTrackResult.SharedPrefsError }
                    }
                    context.getString(R.string.pref_marker_set) -> {
                        trackDao.getTrackWithWayPoints(taskId)
                            .map { it.wayPoints.map { wayPoint -> Pair(wayPoint.longitude, wayPoint.latitude) } }
                            .map<GetTrackResult> { GetTrackResult.SuccessMarkerSet(it) }
                            .onErrorReturn { GetTrackResult.DatabaseCorruptionError }
                    }
                    else -> throw IllegalStateException()
                }
            } else return@flatMap Single.just(GetTrackResult.SharedPrefsError)
        }
        .compose(baseComposers.applySingleSchedulers())

    fun removeTrack(taskId: Long): Single<RemoveTrackResult> = trackDao.delete(taskId)
        .andThen(wayPointDao.delete(taskId))
        .toSingleDefault<RemoveTrackResult>(RemoveTrackResult.Success)
        .onErrorReturn { RemoveTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())

    fun fetchTracks(): Single<FetchTracksResult> = trackDao.getAllTracksWithWayPoints()
        .map<FetchTracksResult> { FetchTracksResult.Success(it) }
        .onErrorReturn { FetchTracksResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())
}

sealed class SaveTrackResult {
    object Success : SaveTrackResult()
    object DatabaseCorruptionError : SaveTrackResult()
}

sealed class GetTrackResult {
    data class SuccessLine(val road: Road) : GetTrackResult()
    data class SuccessMarkerSet(val markerSet: List<Pair<Longitude, Latitude>>) : GetTrackResult()
    object DatabaseCorruptionError : GetTrackResult()
    object SharedPrefsError : GetTrackResult()
}

sealed class RemoveTrackResult {
    object Success : RemoveTrackResult()
    object DatabaseCorruptionError : RemoveTrackResult()
}

sealed class FetchTracksResult {
    data class Success(val tracks: List<TrackWithWayPoints>) : FetchTracksResult()
    object DatabaseCorruptionError : FetchTracksResult()
}