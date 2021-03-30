@file:Suppress("USELESS_CAST")

package com.example.follower.interactors

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import com.example.follower.R
import com.example.follower.db.entities.Track
import com.example.follower.db.entities.WayPoint
import com.example.follower.di.modules.APP_CONTEXT
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
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class TrackInteractor @Inject constructor(
    @Named(APP_CONTEXT) private val context: Context,
    private val trackDao: TrackDao,
    private val wayPointDao: WayPointDao,
    private val logger: FlightRecorder,
    private val baseComposers: BaseComposers,
    private val gson: Gson
) {
    fun deleteTrack(trackId: Long): Single<DeleteTrackResult> = trackDao.delete(trackId)
        .toSingleDefault<DeleteTrackResult> (DeleteTrackResult.Success)
        .onErrorReturn { DeleteTrackResult.DatabaseCorruptionError }
        .doOnError { logger.e("track|wayPoints deleting", error = it) }
        .compose(baseComposers.applySingleSchedulers())

    fun saveWayPoint(wp: WayPoint): Completable = wayPointDao.insert(wp)
        .compose(baseComposers.applyCompletableSchedulers())

    fun renameTrack(track: Track): Single<SaveTrackResult> = trackDao.update(track)
        .toSingleDefault<SaveTrackResult>(SaveTrackResult.Success)
        .onErrorReturn { SaveTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())

    fun saveTrack(track: Track): Single<SaveTrackResult> = trackDao.insert(track)
        .map<SaveTrackResult> { SaveTrackResult.Success }
        .onErrorReturn { SaveTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())

    fun getAddressesList(id: Long): Observable<GetAddressesResult> = trackDao.getTrackWithWayPoints(id)
        .flattenAsObservable {
            logger.i { "getAddresses init size: ${it.wayPoints.size}" }
            it.wayPoints.map { wayPoint -> wayPoint.latitude as Latitude to wayPoint.longitude as Longitude }
        }
        .distinctUntilChanged()
        .map {
            return@map with(Geocoder(context, Locale.getDefault())) {
                val address = kotlin.runCatching { getFromLocation(it.first, it.second, 1).first().getAddressLine(0) }.getOrNull()
                    ?: String.format(context.getString(R.string.address_unknown), it.second, it.first)
                return@with MapPointer(address, it.first, it.second)
            }
        }
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
        .compose(baseComposers.applySingleSchedulers())

    fun fetchTracks(): Single<FetchTracksResult> = trackDao.getAllTracksWithWayPoints()
        .map { it.map { track -> TrackUi(id = track.track.time, title = track.track.title) } }
        .map { if (it.isEmpty()) FetchTracksResult.SuccessEmpty else FetchTracksResult.Success(it) }
        .onErrorReturn { FetchTracksResult.DatabaseCorruptionError }
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

sealed class DeleteTrackResult {
    object Success : DeleteTrackResult()
    object DatabaseCorruptionError : DeleteTrackResult()
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