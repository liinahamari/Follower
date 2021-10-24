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

package dev.liinahamari.follower.networking

import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.TrackWithWayPoints
import dev.liinahamari.follower.db.entities.WayPoint
import dev.liinahamari.follower.helper.ErrorMessageFactory
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class ServerServiceInteractor constructor(
    private val service: ServerService,
    private val baseComposers: BaseComposers,
    private val errorMessageFactory: ErrorMessageFactory,
    private val trackLocalToTrackRemoteMapper: TrackLocalToTrackRemoteMapper
) {
    fun deleteTrack(trackBeginningTime: Long): Single<DeleteTrackResult> = service.delete(trackBeginningTime.toString())
        .map<DeleteTrackResult> { DeleteTrackResult.Success }
        .onErrorReturn { DeleteTrackResult.Failure(errorMessageFactory.errorMessage(it)) }
        .compose(baseComposers.applySingleSchedulers())

    fun getAllTracks(): Single<GetAllTracksResult> = service.getAll()
        .map { it.map(trackLocalToTrackRemoteMapper::transform) }
        .map<GetAllTracksResult> { GetAllTracksResult.Success(it) }
        .onErrorReturn { GetAllTracksResult.Failure(errorMessageFactory.errorMessage(it)) }
        .compose(baseComposers.applySingleSchedulers())

    fun sendTrack(track: TrackWithWayPoints): Single<SendTrackResult> = service.put(trackLocalToTrackRemoteMapper.transform(track))
        .map<SendTrackResult> { SendTrackResult.Success }
        .onErrorReturn { SendTrackResult.Failure(errorMessageFactory.errorMessage(it)) }
        .compose(baseComposers.applySingleSchedulers())

    fun updateTrack(track: TrackWithWayPoints): Single<UpdateTrackResult> = service.replace(trackLocalToTrackRemoteMapper.transform(track), track.track.time)
        .map<UpdateTrackResult> { UpdateTrackResult.Success }
        .onErrorReturn { UpdateTrackResult.Failure(errorMessageFactory.errorMessage(it)) }
        .compose(baseComposers.applySingleSchedulers())
}

class TrackLocalToTrackRemoteMapper @Inject constructor() {
    fun transform(track: TrackWithWayPoints) = ServerTrack(
        time = track.track.time,
        title = track.track.title,
        trackMode = track.track.trackMode,
        wayPoints = track.wayPoints.map { Pair(it.longitude, it.latitude) }.toTypedArray()
    )

    fun transform(track: ServerTrack) = TrackWithWayPoints(
        track = Track(time = track.time, title = track.title, trackMode = track.trackMode),
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


