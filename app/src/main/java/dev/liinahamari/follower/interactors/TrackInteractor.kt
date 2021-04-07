@file:Suppress("USELESS_CAST")

package dev.liinahamari.follower.interactors

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import dev.liinahamari.follower.R
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.TrackWithWayPoints
import dev.liinahamari.follower.db.entities.WayPoint
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.createFileIfNotExist
import dev.liinahamari.follower.ext.getUriForInternalFile
import dev.liinahamari.follower.ext.toReadableDate
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.model.TrackJson
import dev.liinahamari.follower.model.WayPointDao
import dev.liinahamari.follower.model.WayPointJson
import dev.liinahamari.follower.screens.address_trace.MapPointer
import dev.liinahamari.follower.screens.track_list.TrackTitle
import dev.liinahamari.follower.screens.track_list.TrackUi
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.apache.commons.lang3.SerializationException
import java.lang.Exception
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
        .toSingleDefault<DeleteTrackResult>(DeleteTrackResult.Success)
        .onErrorReturn { DeleteTrackResult.DatabaseCorruptionError }
        .doOnError { logger.e("track|wayPoints deleting", error = it) }
        .compose(baseComposers.applySingleSchedulers())

    fun saveWayPoint(wp: WayPoint): Completable = wayPointDao.insert(wp)
        .compose(baseComposers.applyCompletableSchedulers())

    private fun saveWayPoints(wp: List<WayPoint>): Completable = wayPointDao.insertAll(wp)
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
            it.wayPoints
        }
        .distinctUntilChanged()
        .map {
            return@map with(Geocoder(context, Locale.getDefault())) {
                val address = kotlin.runCatching { getFromLocation(it.latitude, it.longitude, 1).first().getAddressLine(0) }.getOrNull()
                    ?: String.format(context.getString(R.string.address_unknown), it.longitude, it.latitude)
                return@with MapPointer(address, it.latitude, it.longitude, it.time.toReadableDate())
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

    /** @param isTracking -- hides last track if true*/
    fun fetchTracks(isTracking: Boolean): Single<FetchTracksResult> = trackDao.getAllTracksWithWayPoints()
        .map { if (isTracking) it.dropLast(1) else it }
        .map { it.map { track -> TrackUi(id = track.track.time, title = track.track.title, isImported = track.track.isImported) } }
        .map { if (it.isEmpty()) FetchTracksResult.SuccessEmpty else FetchTracksResult.Success(it) }
        .onErrorReturn { FetchTracksResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())

    /*todo: caching list of fetched tracks as a field?*/
    /*TODO: rethink!*/
    fun getTrackJsonFile(trackId: Long, fileExtension: String): Single<SharedTrackResult> = trackDao.getTrackWithWayPoints(trackId)
        .map {
            TrackJson(
                title = it.track.title,
                time = it.track.time,
                wayPoints = it.wayPoints.map { wp ->
                    WayPointJson(
                        trackId = wp.trackId,
                        provider = wp.provider,
                        latitude = wp.latitude,
                        longitude = wp.longitude,
                        time = wp.time
                    )
                }.toTypedArray()
            )
        }
        .map {
            context.getUriForInternalFile(
                context.createFileIfNotExist(
                    fileName = it.title + fileExtension,
                    dirName = "TempTracksToShare"
                )
                    .apply {
                        writeText(gson.toJson(it))
                    }
            ) to it.title
        }
        .map<SharedTrackResult> { SharedTrackResult.Success(it) }
        .onErrorReturn { SharedTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())

    fun importTracks(fileUri: Uri, isTracking: Boolean): Single<ImportTrackResult> = Single.fromCallable {
        context.contentResolver.openInputStream(fileUri)?.use {
            it.bufferedReader().readText()
        }
    }
        .map {
            try {
                gson.fromJson(it, TrackJson::class.java).also { track ->
                    if (track.time == 0L || track.wayPoints.isEmpty() || track.wayPoints.any { wp -> wp.time == 0L || wp.trackId < 1 }) {
                        throw SerializationException()
                    }
                }
            } catch (e: Exception) {
                throw SerializationException()
            }
        }
        .map {
            TrackWithWayPoints(Track(it.time, it.title, true), it.wayPoints.map { WayPoint(trackId = it.trackId, provider = it.provider, longitude = it.longitude, latitude = it.latitude, time = it.time) })
        }
        .flatMap { track -> saveTrack(track.track).flatMapCompletable { saveWayPoints(track.wayPoints) }.andThen(fetchTracks(isTracking)) }
        .map {
            when (it) {
                is FetchTracksResult.Success -> ImportTrackResult.Success(it.tracks)
                is FetchTracksResult.DatabaseCorruptionError -> ImportTrackResult.DatabaseCorruptionError
                is FetchTracksResult.SuccessEmpty -> throw RuntimeException()
            }
        }
        .onErrorReturn {
            logger.e("Track import", it)
            when (it) {
                is SerializationException -> ImportTrackResult.ParsingError
                else -> ImportTrackResult.CommonError
            }

        }
        .compose(baseComposers.applySingleSchedulers())
}

sealed class SaveTrackResult {
    object Success : SaveTrackResult()
    object DatabaseCorruptionError : SaveTrackResult()
}

sealed class ImportTrackResult {
    data class Success(val tracks: List<TrackUi>) : ImportTrackResult()
    object DatabaseCorruptionError : ImportTrackResult()
    object ParsingError : ImportTrackResult()
    object CommonError : ImportTrackResult()
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