package com.example.follower.screens.tracking_control

import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.db.entities.Track
import com.example.follower.db.entities.WayPoint
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.SaveTrackResult
import com.example.follower.interactors.TrackInteractor
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class TrackingControlViewModel @Inject constructor(private val trackInteractor: TrackInteractor) : BaseViewModel() {
    private val _saveTrackEvent = SingleLiveEvent<Int>()
    val saveTrackEvent: LiveData<Int> get() = _saveTrackEvent

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    fun saveTrack(time: Long, title: String, wayPoints: List<WayPoint>) {
        disposable += trackInteractor.saveTrack(Track(time, title), wayPoints)
            .subscribe(Consumer {
                when (it) {
                    is SaveTrackResult.Success -> _saveTrackEvent.value = R.string.toast_track_saved
                    is SaveTrackResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
                }
            })
    }
}