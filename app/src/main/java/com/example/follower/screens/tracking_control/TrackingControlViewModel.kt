package com.example.follower.screens.tracking_control

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.db.Track
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.SaveTrackResult
import com.example.follower.interactors.TrackInteractor
import com.example.follower.screens.map.WayPoint
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class TrackingControlViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val context: Context) : BaseViewModel() {
    private val _saveTrackEvent = SingleLiveEvent<String>()
    val saveTrackEvent: LiveData<String> get() = _saveTrackEvent

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    fun saveTrack(time: Long, title: String, wayPoints: List<WayPoint>) {
        disposable += trackInteractor.saveTrack(Track(time, title), wayPoints)
            .subscribe(Consumer {
                when (it) {
                    is SaveTrackResult.Success -> _saveTrackEvent.value = context.getString(R.string.toast_track_saved)
                    is SaveTrackResult.DatabaseCorruptionError -> _errorEvent.value = context.getString(R.string.db_error)
                }
            })
    }
}