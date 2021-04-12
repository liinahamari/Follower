package dev.liinahamari.follower.screens.trace_map

import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.plusAssign
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.Polyline
import javax.inject.Inject

typealias Longitude = Double
typealias Latitude = Double

@RoadBuildingScope
class TraceFragmentViewModel @Inject constructor(private val roadBuildingInteractor: RoadBuildingInteractor): BaseViewModel() {
    private val _getTrackAsLineEvent = SingleLiveEvent<TrackUi.Road>()
    val getTrackAsLineEvent: LiveData<TrackUi.Road> get() = _getTrackAsLineEvent

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
}

sealed class TrackUi {
    abstract val startPoint: WayPointUi
    abstract val finishPoint: WayPointUi
    abstract val boundingBox: BoundingBox

    data class Markers(val wayPoints: List<WayPointUi>, override val startPoint: WayPointUi, override val finishPoint: WayPointUi, override val boundingBox: BoundingBox): TrackUi()
    data class Road(val road: Polyline, override val startPoint: WayPointUi, override val finishPoint: WayPointUi, override val boundingBox: BoundingBox): TrackUi()
}

data class WayPointUi(val lat: Latitude, val lon: Longitude, val readableTimeStamp: String)