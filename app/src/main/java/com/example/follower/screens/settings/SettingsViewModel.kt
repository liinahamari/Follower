package com.example.follower.screens.settings

import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.AutoTrackingSchedulingUseCase
import com.example.follower.interactors.ResetToDefaultsState
import com.example.follower.interactors.SchedulingStartStopResult
import com.example.follower.interactors.SettingsPrefsInteractor
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val prefInteractor: SettingsPrefsInteractor, private val autoTrackingSchedulingUseCase: AutoTrackingSchedulingUseCase /*todo dagger scope*/) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _successfulSchedulingEvent = SingleLiveEvent<Int>()
    val successfulSchedulingEvent: LiveData<Int> get() = _successfulSchedulingEvent

    private val _resetToDefaultsEvent = SingleLiveEvent<Any>()
    val resetToDefaultsEvent: LiveData<Any> get() = _resetToDefaultsEvent

    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    fun resetOptionsToDefaults() {
        disposable += prefInteractor.resetOptionsToDefaults().subscribe {
            when (it) {
                is ResetToDefaultsState.Success -> {
                    _loadingEvent.value = false
                    _resetToDefaultsEvent.call()
                }
                is ResetToDefaultsState.Failure -> {
                    _loadingEvent.value = false
                    _errorEvent.value = R.string.error_unexpected
                }
                is ResetToDefaultsState.Loading -> _loadingEvent.value = true
            }
        }
    }

    fun scheduleAutoTracking() {
        disposable += autoTrackingSchedulingUseCase.setupStartAndStop().subscribe(Consumer {
            when (it) {
                is SchedulingStartStopResult.Success -> _successfulSchedulingEvent.value = R.string.auto_tracking_scheduling_successful
                is SchedulingStartStopResult.Failure -> _errorEvent.value = R.string.error_unexpected
            }
        })
    }
}