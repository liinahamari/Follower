package dev.liinahamari.follower.screens.settings

import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

@SettingsScope
class SettingsViewModel @Inject constructor(private val prefInteractor: SettingsPrefsInteractor,
                                            private val autoTrackingSchedulingUseCase: AutoTrackingSchedulingUseCase,
                                            private val biometricValidationUseCase: BiometricAvailabilityValidationUseCase
) : BaseViewModel() {
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

    private val _biometricNotAvailableEvent = SingleLiveEvent<Int>()
    val biometricNotAvailableEvent: LiveData<Int> get() = _biometricNotAvailableEvent

    fun isBiometricValidationAvailable() {
        disposable += biometricValidationUseCase.execute().subscribe(Consumer {
            if (it is BiometricAvailabilityResult.NotAvailable) {
                _biometricNotAvailableEvent.value = it.explanation
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