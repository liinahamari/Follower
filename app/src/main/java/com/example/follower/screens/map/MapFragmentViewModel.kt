package com.example.follower.screens.map

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.GetTrackResult
import com.example.follower.interactors.TrackInteractor
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import org.osmdroid.bonuspack.routing.Road
import javax.inject.Inject

typealias Longitude = Double
typealias Latitude = Double

class MapFragmentViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val context: Context): BaseViewModel() {
    private val _getTrackAsLine = SingleLiveEvent<Road>()
    val getTrackAsLine: LiveData<Road> get() = _getTrackAsLine

    private val _getTrackAsMarkerSet = SingleLiveEvent<List<Pair<Longitude, Latitude>>>()
    val getTrackAsMarkerSet: LiveData<List<Pair<Longitude, Latitude>>> get() = _getTrackAsMarkerSet

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    fun getTrack(trackId: Long) {
        disposable += trackInteractor.getTrackById(trackId)
            .subscribe(Consumer {
                when (it) {
                    is GetTrackResult.SuccessLine -> _getTrackAsLine.value = it.road
                    is GetTrackResult.SuccessMarkerSet -> _getTrackAsMarkerSet.value = it.markerSet
                    else -> _errorEvent.value = context.getString(R.string.db_error)
                }
            })
    }
}