package com.example.follower.networking

import com.example.follower.db.entities.Track
import com.example.follower.db.entities.TrackWithWayPoints
import com.example.follower.db.entities.WayPoint
import com.example.follower.helper.ErrorMessageFactory
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Single
import javax.inject.Inject

class ServerServiceInteractor constructor(private val service: ServerService, private val baseComposers: BaseComposers, private val errorMessageFactory: ErrorMessageFactory, private val trackLocalToTrackRemoteMapper: TrackLocalToTrackRemoteMapper) {
    fun deleteTrack(trackBeginningTime: Long): Single<DeleteTrackResult> = service.delete(trackBeginningTime.toString())
        .map<DeleteTrackResult> { DeleteTrackResult.Success }
        .onErrorReturn { DeleteTrackResult.Failure(errorMessageFactory.errorMessage(it)) }
        .compose(baseComposers.applySingleSchedulers())
        .doOnError { it.printStackTrace() }

    fun getAllTracks(): Single<GetAllTracksResult> = service.getAll()
        .map { it.map(trackLocalToTrackRemoteMapper::transform) }
        .map<GetAllTracksResult> { GetAllTracksResult.Success(it) }
        .onErrorReturn { GetAllTracksResult.Failure(errorMessageFactory.errorMessage(it)) }
        .compose(baseComposers.applySingleSchedulers())
        .doOnError { it.printStackTrace() }

    fun sendTrack(track: TrackWithWayPoints): Single<SendTrackResult> = service.put(trackLocalToTrackRemoteMapper.transform(track))
        .map<SendTrackResult> { SendTrackResult.Success }
        .onErrorReturn { SendTrackResult.Failure(errorMessageFactory.errorMessage(it)) }
        .compose(baseComposers.applySingleSchedulers())
        .doOnError { it.printStackTrace() }

    fun updateTrack(track: TrackWithWayPoints): Single<UpdateTrackResult> = service.replace(trackLocalToTrackRemoteMapper.transform(track), track.track.time)
        .map<UpdateTrackResult> { UpdateTrackResult.Success }
        .onErrorReturn { UpdateTrackResult.Failure(errorMessageFactory.errorMessage(it)) }
        .compose(baseComposers.applySingleSchedulers())
        .doOnError { it.printStackTrace() }
}

class TrackLocalToTrackRemoteMapper @Inject constructor() {
    fun transform(track: TrackWithWayPoints) = TrackRequest(
        time = track.track.time,
        title = track.track.title,
        wayPoints = track.wayPoints.map { Pair(it.longitude, it.latitude) }.toTypedArray()
    )

    fun transform(track: TrackRequest) = TrackWithWayPoints(
        track = Track(track.time, track.title),
        wayPoints = track.wayPoints.map { WayPoint(trackId = track.time, longitude = it.first, latitude = it.second, provider = "TODO", time = 1L /*TODO*/) }
    )
}

sealed class DeleteTrackResult {
    object Success : DeleteTrackResult()
    data class Failure(val errorMessage: String) : DeleteTrackResult()
}

sealed class UpdateTrackResult {
    object Success : UpdateTrackResult()
    data class Failure(val errorMessage: String) : UpdateTrackResult()
}

sealed class SendTrackResult {
    object Success : SendTrackResult()
    data class Failure(val errorMessage: String) : SendTrackResult()
}

sealed class GetAllTracksResult {
    data class Success(val tracks: List<TrackWithWayPoints>) : GetAllTracksResult()
    data class Failure(val errorMessage: String) : GetAllTracksResult()
}


