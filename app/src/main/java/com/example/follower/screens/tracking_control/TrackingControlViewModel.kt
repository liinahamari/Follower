package com.example.follower.screens.tracking_control

import androidx.lifecycle.LiveData
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class TrackingControlViewModel @Inject constructor(private val clearWayPointsInteractor: ClearWayPointsInteractor) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _stopServiceEvent = SingleLiveEvent<Any>()
    val stopServiceEvent: LiveData<Any> get() = _stopServiceEvent

    fun clearWaypoints(trackId: Long) {
        disposable += clearWayPointsInteractor.clearWayPoints(trackId)
            .subscribe(Consumer { _stopServiceEvent.call() })
    }
}