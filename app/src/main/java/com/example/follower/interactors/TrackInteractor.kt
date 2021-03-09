@file:Suppress("USELESS_CAST")

package com.example.follower.interactors

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import com.example.follower.R
import com.example.follower.db.entities.Track
import com.example.follower.db.entities.WayPoint
import com.example.follower.ext.createFileIfNotExist
import com.example.follower.ext.getUriForInternalFile
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.model.TrackDao
import com.example.follower.model.WayPointDao
import com.example.follower.screens.address_trace.MapPointer
import com.example.follower.screens.trace_map.Latitude
import com.example.follower.screens.trace_map.Longitude
import com.example.follower.screens.track_list.TrackTitle
import com.example.follower.screens.track_list.TrackUi
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class TrackInteractor @Inject constructor(
    private val context: Context,
    private val trackDao: TrackDao,
    private val wayPointDao: WayPointDao,
    private val logger: FlightRecorder,
    private val baseComposers: BaseComposers,
    private val gson: Gson
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
        .doOnError { it.printStackTrace() }
        .compose(baseComposers.applySingleSchedulers())
        .doOnSuccess { logger.i { "Track saved with ${wayPoints.size} wayPoints" } }

    fun getAddressesList(id: Long): Observable<GetAddressesResult> = trackDao.getTrackWithWayPoints(id)
        .flattenAsObservable {
            logger.i { "getAddresses init size: ${it.wayPoints.size}" }
            it.wayPoints.map { wayPoint -> wayPoint.latitude as Latitude to wayPoint.longitude as Longitude }
        }
        .distinctUntilChanged()
        .map {
            return@map with(Geocoder(context, Locale.getDefault())) {
                val address = kotlin.runCatching { getFromLocation(it.first, it.second, 1).first().getAddressLine(0) }.getOrNull() ?: String.format(context.getString(R.string.address_unknown), it.second, it.first)
                MapPointer(address, it.first, it.second)
            }
        }
        .doOnError { it.printStackTrace() }
        .toList()
        .toObservable()
        .doOnNext { logger.i { "getAddresses trimmed size: ${it.size}" } }
        .map<GetAddressesResult> { GetAddressesResult.Success(it) }
        .onErrorReturn { GetAddressesResult.DatabaseCorruptionError }
        .startWith(GetAddressesResult.Loading)
        .compose(baseComposers.applyObservableSchedulers())

    fun removeTrack(taskId: Long): Single<RemoveTrackResult> = trackDao.delete(taskId)
        .andThen(wayPointDao.delete(taskId))
        .toSingleDefault<RemoveTrackResult>(RemoveTrackResult.Success)
        .onErrorReturn { RemoveTrackResult.DatabaseCorruptionError }
        .doOnError { it.printStackTrace() }
        .compose(baseComposers.applySingleSchedulers())

    fun fetchTracks(): Single<FetchTracksResult> = trackDao.getAllTracksWithWayPoints()
        .map { it.map { track -> TrackUi(id = track.track.time, title = track.track.title) } }
        .map { if (it.isEmpty()) FetchTracksResult.SuccessEmpty else FetchTracksResult.Success(it) }
        .onErrorReturn { FetchTracksResult.DatabaseCorruptionError }
        .doOnError { it.printStackTrace() }
        .compose(baseComposers.applySingleSchedulers())

    /*todo: caching list of fetched tracks as a field?*/
    /*TODO: rethink!*/
    fun getTrackJsonFile(trackId: Long, fileExtension: String): Single<SharedTrackResult> = trackDao.getTrackWithWayPoints(trackId)
        .map {
            context.getUriForInternalFile(
                context.createFileIfNotExist(
                    fileName = it.track.title+fileExtension,
                    dirName = "TempTracksToShare"
                )
                    .apply {
                        writeText(gson.toJson(it))
                    }
            ) to it.track.title
        }
        .map<SharedTrackResult> { SharedTrackResult.Success(it) }
        .onErrorReturn { SharedTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())
        .doOnError { it.printStackTrace() }
}

sealed class SaveTrackResult {
    object Success : SaveTrackResult()
    object DatabaseCorruptionError : SaveTrackResult()
}

sealed class GetAddressesResult {
    data class Success(val addresses: List<MapPointer>) : GetAddressesResult()
    object DatabaseCorruptionError : GetAddressesResult()
    object Loading : GetAddressesResult()
}

sealed class ClearWayPointsResult {
    object Success : ClearWayPointsResult()
    object DatabaseCorruptionError : ClearWayPointsResult()
}

sealed class RemoveTrackResult {
    object Success : RemoveTrackResult()
    object DatabaseCorruptionError : RemoveTrackResult()
}

sealed class FetchTracksResult {
    data class Success(val tracks: List<TrackUi>) : FetchTracksResult()
    object SuccessEmpty : FetchTracksResult()
    object DatabaseCorruptionError : FetchTracksResult()
}

sealed class SharedTrackResult {
    data class Success(val trackJsonAndTitle: Pair<Uri, TrackTitle>) : SharedTrackResult()
    object DatabaseCorruptionError : SharedTrackResult()
}