package com.example.follower.screens.settings

import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.di.scopes.SettingsScope
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.*
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

@SettingsScope
class SettingsViewModel @Inject constructor(private val prefInteractor: SettingsPrefsInteractor,
                                            private val autoTrackingSchedulingUseCase: AutoTrackingSchedulingUseCase,
                                            private val biometricValidationUseCase: BiometricAvailabilityValidationUseCase) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _successfulSchedulingEvent = SingleLiveEvent<Int>()
    val successfulSchedulingEvent: LiveData<Int> get() = _successfulSchedulingEvent

    private val _autoTrackingCancellingEvent = SingleLiveEvent<Int>()
    val autoTrackingCancellingEvent: LiveData<Int> get() = _autoTrackingCancellingEvent

    private val _resetToDefaultsEvent = SingleLiveEvent<Any>()
    val resetToDefaultsEvent: LiveData<Any> get() = _resetToDefaultsEvent

    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    private val _biometricNotAvailable = SingleLiveEvent<Int>()
    val biometricNotAvailable: LiveData<Int> get() = _biometricNotAvailable

    fun isBiometricValidationAvailable() {
        disposable += biometricValidationUseCase.execute().subscribe(Consumer {
            if (it is BiometricAvailabilityResult.NotAvailable) {
                _biometricNotAvailable.value = it.explanation
            }
        })
    }

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

    fun cancelAutoTracking() {
        disposable += autoTrackingSchedulingUseCase.cancelAutoTracking().subscribe(Consumer {
            when (it) {
                is CancelAutoTrackingResult.Success -> _autoTrackingCancellingEvent.value = R.string.auto_tracking_cancelling_successful
                is CancelAutoTrackingResult.Failure -> _errorEvent.value = R.string.error_cant_stop_auto_tacking
            }
        })
    }
}