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
import javax.inject.Inject

typealias Longitude = Double
typealias Latitude = Double

class MapFragmentViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val context: Context): BaseViewModel() {
    private val _getTrackEvent = SingleLiveEvent<List<Pair<Longitude, Latitude>>>()
    val getTrackEvent: LiveData<List<Pair<Longitude, Latitude>>> get() = _getTrackEvent

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    fun getTrack(trackId: Long){
        disposable += trackInteractor.getTrackById(trackId)
            .subscribe(Consumer {
                when (it) {
                    is GetTrackResult.Success -> _getTrackEvent.value = it.waypoints.map { wayPoint ->  Pair(wayPoint.longitude, wayPoint.latitude) }
                    is GetTrackResult.DatabaseCorruptionError -> _errorEvent.value = context.getString(R.string.db_error)
                }
            })
    }
}