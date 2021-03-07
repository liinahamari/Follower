package com.example.follower.screens.tracking_control

import androidx.lifecycle.LiveData
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.TrackInteractor
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class TrackingControlViewModel @Inject constructor(private val trackInteractor: TrackInteractor) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _stopServiceEvent = SingleLiveEvent<Any>()
    val stopServiceEvent: LiveData<Any> get() = _stopServiceEvent

    fun clearWaypoints(trackId: Long) {
        disposable += trackInteractor.clearWayPoints(trackId)
            .subscribe(Consumer { _stopServiceEvent.call() })
    }
}