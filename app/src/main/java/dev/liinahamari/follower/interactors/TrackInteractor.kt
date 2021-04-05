@file:Suppress("USELESS_CAST")

package dev.liinahamari.follower.interactors

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.google.gson.Gson
import dev.liinahamari.follower.R
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.WayPoint
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.createFileIfNotExist
import dev.liinahamari.follower.ext.getUriForInternalFile
import dev.liinahamari.follower.ext.toReadableDate
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.model.WayPointDao
import dev.liinahamari.follower.screens.address_trace.MapPointer
import dev.liinahamari.follower.screens.track_list.TrackTitle
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
        .toSingleDefault<DeleteTrackResult>(DeleteTrackResult.Success)
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
        .toSingle {  trackDao.getCount() == 0 }
        .map<RemoveTrackResult> { RemoveTrackResult.Success(it) }
        .onErrorReturn { RemoveTrackResult.DatabaseCorruptionError }
        .compose(baseComposers.applySingleSchedulers())

    /** @param isTracking -- hides last track if true*/
    fun fetchTracks(isTracking: Boolean, trackIdToExclude: Long?): Observable<FetchTracksResult> = RxPagedListBuilder(
        trackDao.getAll(), PagedList.Config.Builder()
            .setPageSize(30)
            .setInitialLoadSizeHint(30)
            .setPrefetchDistance(10)
            .setEnablePlaceholders(false)
            .build()
    ).buildObservable()
        .map { if (isTracking) it.apply { removeIf { track -> track.time == trackIdToExclude } } else it }
        .map { if (it.isEmpty()) FetchTracksResult.SuccessEmpty else FetchTracksResult.Success(it) }
        .onErrorReturn { FetchTracksResult.DatabaseCorruptionError }
        .compose(baseComposers.applyObservableSchedulers())
        .doOnError { it.printStackTrace() }

    /*todo: caching list of fetched tracks as a field?*/
    /*TODO: rethink!*/
    fun getTrackJsonFile(trackId: Long, fileExtension: String): Single<SharedTrackResult> = trackDao.getTrackWithWayPoints(trackId)
        .map {
            context.getUriForInternalFile(
                context.createFileIfNotExist(
                    fileName = it.track.title + fileExtension,
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
    data class Success(val isTracksEmpty: Boolean) : RemoveTrackResult()
    object DatabaseCorruptionError : RemoveTrackResult()
}

sealed class FetchTracksResult {
    data class Success(val tracks: PagedList<Track>) : FetchTracksResult()
    object SuccessEmpty : FetchTracksResult()
    object DatabaseCorruptionError : FetchTracksResult()
}

sealed class SharedTrackResult {
    data class Success(val trackJsonAndTitle: Pair<Uri, TrackTitle>) : SharedTrackResult()
    object DatabaseCorruptionError : SharedTrackResult()
}