package com.example.follower.screens.trace_map

import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

typealias Longitude = Double
typealias Latitude = Double

@RoadBuildingScope
class TraceFragmentViewModel @Inject constructor(private val roadBuildingInteractor: RoadBuildingInteractor): BaseViewModel() {
    private val _getTrackAsLineEvent = SingleLiveEvent<Road>()
    val getTrackAsLineEvent: LiveData<Road> get() = _getTrackAsLineEvent

    private val _getTrackAsMarkerSet = SingleLiveEvent<List<Pair<GeoPoint, String>>>()
    val getTrackAsMarkerSet: LiveData<List<Pair<GeoPoint, String>>> get() = _getTrackAsMarkerSet

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

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

