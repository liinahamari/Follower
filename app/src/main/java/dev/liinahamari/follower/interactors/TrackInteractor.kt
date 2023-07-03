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

package dev.liinahamari.follower.interactors

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import com.google.gson.Gson
import dev.liinahamari.follower.R
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.TrackWithWayPoints
import dev.liinahamari.follower.db.entities.WayPoint
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.createFileIfNotExist
import dev.liinahamari.follower.ext.getTrackLength
import dev.liinahamari.follower.ext.getUriForInternalFile
import dev.liinahamari.follower.ext.toReadableDate
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.model.TrackJson
import dev.liinahamari.follower.model.WayPointDao
import dev.liinahamari.follower.model.WayPointJson
import dev.liinahamari.follower.model.toMean
import dev.liinahamari.follower.screens.address_trace.MapPointer
import dev.liinahamari.follower.screens.track_list.TrackTitle
import dev.liinahamari.follower.screens.track_list.TrackUi
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class TrackInteractor @Inject constructor(
    @Named(APP_CONTEXT) private val context: Context,
    private val trackDao: TrackDao,
    private val wayPointDao: WayPointDao,
    private val baseComposers: BaseComposers,
    private val gson: Gson,
    private val osmRoadManager: OSRMRoadManager
) {
    fun deleteTrack(trackId: Long): Single<DeleteTrackResult> = trackDao.delete(trackId)
        .toSingleDefault<DeleteTrackResult>(DeleteTrackResult.Success)
        .onErrorReturn { DeleteTrackResult.DatabaseCorruptionError }

    fun saveWayPoint(wp: WayPoint): Completable = wayPointDao.insert(wp)

    private fun saveWayPoints(wp: List<WayPoint>): Completable = wayPointDao.insertAll(wp)
        .compose(baseComposers.applyCompletableSchedulers())

    /** To validate equity of waypoints put in database and in the field of LocationTrackingService (issue with database) */
    fun getWayPointsById(trackId: Long): Single<Int> = wayPointDao.validateWpAmount(trackId)
        .map { it.size }

    fun renameTrack(trackId: Long, title: String): Single<SaveTrackResult> =
        trackDao.getTrackWithWayPoints(trackId)
            .flatMap {
                val length = osmRoadManager.getTrackLength(it.wayPoints, it.track.trackMode.toMean())
                trackDao.update(it.track.copy(title = title, length = length))
                    .toSingleDefault<SaveTrackResult>(SaveTrackResult.Success)
                    .onErrorReturn { SaveTrackResult.DatabaseCorruptionError }
            }
            .onErrorReturn { SaveTrackResult.DatabaseCorruptionError }

    fun saveTrack(track: Track): Single<SaveTrackResult> = trackDao.insert(track)
        .map<SaveTrackResult> { SaveTrackResult.Success }
        .onErrorReturn { SaveTrackResult.DatabaseCorruptionError }

    fun getAddressesList(id: Long): Observable<GetAddressesResult> = trackDao.getTrackWithWayPoints(id)
        .flattenAsObservable {
            FlightRecorder.i { "getAddresses init size: ${it.wayPoints.size}" }
            it.wayPoints
        }
        .distinctUntilChanged()
        .map {
            return@map with(Geocoder(context, Locale.getDefault())) {
                val address = kotlin.runCatching { getFromLocation(it.latitude, it.longitude, 1)!!.first().getAddressLine(0) }.getOrNull()
                    ?: String.format(context.getString(R.string.address_unknown), it.longitude, it.latitude)
                return@with MapPointer(address, it.latitude, it.longitude, it.time.toReadableDate())
            }
        }
        .toList()
        .toObservable()
        .doOnNext { FlightRecorder.i { "getAddresses trimmed size: ${it.size}" } }
        .map<GetAddressesResult> { GetAddressesResult.Success(it) }
        .onErrorReturn { GetAddressesResult.DatabaseCorruptionError }
        .startWithItem(GetAddressesResult.Loading)
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
                }.toTypedArray(),
                trackMode = it.track.trackMode,
                trackLength = it.track.length
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
                gson.fromJson(it, TrackJson::class.java)
                    .also { track ->
                        if (track.time == 0L || track.wayPoints.isEmpty() || track.wayPoints.any { wp -> wp.time == 0L || wp.trackId < 1 }) {
                            throw JsonParsingException()
                        }
                    }
            } catch (e: Exception) {
                throw JsonParsingException()
            }
        }.flatMap { trackJson ->
            trackDao.getAllIds()
                .flatMap { ids ->
                    if (ids.none { it == trackJson.time }) Single.just(trackJson) else throw EntityAlreadyPresentedError() //TODO. future feature: update dialog
                }
        }.map {
            TrackWithWayPoints(Track(it.time, it.title, true, it.trackMode, it.trackLength), it.wayPoints.map { wp -> WayPoint(trackId = wp.trackId, provider = wp.provider, longitude = wp.longitude, latitude = wp.latitude, time = wp.time) })
        }.flatMap { track ->
            saveTrack(track.track).flatMapCompletable { saveWayPoints(track.wayPoints) }.andThen(fetchTracks(isTracking))
        }.map {
            when (it) {
                is FetchTracksResult.Success -> ImportTrackResult.Success(it.tracks)
                is FetchTracksResult.DatabaseCorruptionError -> ImportTrackResult.DatabaseCorruptionError
                is FetchTracksResult.SuccessEmpty -> throw RuntimeException()
            }
        }.onErrorReturn {
            when (it) {
                is JsonParsingException -> ImportTrackResult.ParsingError
                is EntityAlreadyPresentedError -> ImportTrackResult.EntityAlreadyPresentedError
                else -> ImportTrackResult.CommonError
            }

        }.compose(baseComposers.applySingleSchedulers("Track import"))
}

sealed class SaveTrackResult {
    object Success : SaveTrackResult()
    object DatabaseCorruptionError : SaveTrackResult()
}

sealed class ImportTrackResult {
    data class Success(val tracks: List<TrackUi>) : ImportTrackResult()
    object DatabaseCorruptionError : ImportTrackResult()
    object ParsingError : ImportTrackResult()
    object EntityAlreadyPresentedError : ImportTrackResult()
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

class EntityAlreadyPresentedError : Exception()
class JsonParsingException : Exception()
