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

import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.interactors.TrackInteractor
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.plusAssign
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import javax.inject.Inject

typealias Longitude = Double
typealias Latitude = Double

@RoadBuildingScope
class TraceFragmentViewModel @Inject constructor(
    private val roadBuildingInteractor: RoadBuildingInteractor,
    private val trackInteractor: TrackInteractor
) : BaseViewModel() {
    private val _getAllTracksAsLineEvent = SingleLiveEvent<TracksUi>()
    val getAllTrackAsLineEvent: LiveData<TracksUi> get() = _getAllTracksAsLineEvent

    private val _getTrackAsLineEvent = SingleLiveEvent<TrackUi.Road>()
    val getTrackAsLineEvent: LiveData<TrackUi.Road> get() = _getTrackAsLineEvent

    private val _updateTrackLengthEvent = SingleLiveEvent<Double>()
    val updateTrackLengthEvent: LiveData<Double> get() = _updateTrackLengthEvent

    private val _getTrackAsMarkerSet = SingleLiveEvent<TrackUi.Markers>()
    val getTrackAsMarkerSet: LiveData<TrackUi.Markers> get() = _getTrackAsMarkerSet

    fun getTrack(trackId: Long) {
        disposable += roadBuildingInteractor.getRoad(trackId)
            .subscribe(Consumer {
                when (it) {
                    is GetRoadResult.SuccessfulLine -> _getTrackAsLineEvent.value = it.road
                    is GetRoadResult.SuccessfulMarkerSet -> _getTrackAsMarkerSet.value = it.markerSet
                    else -> _errorEvent.value = R.string.db_error
                }
            })
    }

    fun getAllTracks() {
        disposable += roadBuildingInteractor.getAllRoads()
            .subscribe(Consumer {
                when (it) {
                    is GetAllRoadsResult.SuccessfulLine -> _getAllTracksAsLineEvent.value = it.roads
                    else -> _errorEvent.value = R.string.db_error
                }
            })
    }

    fun updateTrackLength(length: CharSequence, trackId: Long) {
        disposable += roadBuildingInteractor.updateLength(length.toString().toDouble(), trackId)
            .subscribe(Consumer {
                when (it) {
                    is UpdateTrackLengthResult.Success -> _updateTrackLengthEvent.value = length.toString().toDouble()
                    is UpdateTrackLengthResult.Error -> _errorEvent.value = R.string.error_updating_track_length
                    else -> _errorEvent.value = R.string.db_error
                }
            })
    }

    fun extendTrack(geoPoint: GeoPoint, trackId: Long) {
        disposable += trackInteractor.extendTrack(trackId, geoPoint)
            .andThen(roadBuildingInteractor.getRoad(trackId))
            .subscribe { it ->
                when (it) {
                    is GetRoadResult.SuccessfulLine -> _getTrackAsLineEvent.value = it.road
                    is GetRoadResult.SuccessfulMarkerSet -> _getTrackAsMarkerSet.value = it.markerSet
                    else -> _errorEvent.value = R.string.db_error
                }
            }
    }
}

sealed class TrackUi {
    abstract val startPoint: WayPointUi
    abstract val finishPoint: WayPointUi
    abstract val boundingBox: BoundingBox

    data class Markers(
        val wayPoints: List<WayPointUi>,
        override val startPoint: WayPointUi,
        override val finishPoint: WayPointUi,
        override val boundingBox: BoundingBox
    ) : TrackUi()

    data class Road(
        val trackId: Long,
        val road: Polyline,
        override val startPoint: WayPointUi,
        override val finishPoint: WayPointUi,
        override val boundingBox: BoundingBox,
        val length: Double
    ) : TrackUi()
}

data class TracksUi(val boundingBox: BoundingBox, val roads: List<Polyline>, val length: Double)

data class WayPointUi(val lat: Latitude, val lon: Longitude, val readableTimeStamp: String)
