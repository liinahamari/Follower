package dev.liinahamari.follower.screens.trace_map

import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import org.osmdroid.bonuspack.routing.Road
import javax.inject.Inject

typealias Longitude = Double
typealias Latitude = Double

@RoadBuildingScope
class TraceFragmentViewModel @Inject constructor(private val roadBuildingInteractor: RoadBuildingInteractor): BaseViewModel() {
    private val _getTrackAsLine = SingleLiveEvent<Road>()
    val getTrackAsLine: LiveData<Road> get() = _getTrackAsLine

    private val _getTrackAsMarkerSet = SingleLiveEvent<List<Pair<Longitude, Latitude>>>()
    val getTrackAsMarkerSet: LiveData<List<Pair<Longitude, Latitude>>> get() = _getTrackAsMarkerSet

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    fun getTrack(trackId: Long) {
        disposable += roadBuildingInteractor.getRoad(trackId)
            .subscribe(Consumer {
                when (it) {
                    is GetRoadResult.SuccessfulLine -> _getTrackAsLine.value = it.road
                    is GetRoadResult.SuccessfulMarkerSet -> _getTrackAsMarkerSet.value = it.markerSet
                    else -> _errorEvent.value = R.string.db_error
                }
            })
    }
}