package dev.liinahamari.follower.screens.settings

import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

@SettingsScope
class SettingsViewModel @Inject constructor(
    private val prefToDefaultsInteractor: ResetPrefsToDefaultsInteractor,
    private val autoTrackingSchedulingUseCase: AutoTrackingSchedulingUseCase,
    private val biometricValidationUseCase: BiometricAvailabilityValidationUseCase,
    private val purgeCacheUseCase: PurgeCacheUseCase
) : BaseViewModel() {
    private val _operationSucceededEvent = SingleLiveEvent<Int>()
    val operationSucceededEvent: LiveData<Int> get() = _operationSucceededEvent

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
        disposable += prefToDefaultsInteractor.resetPrefsToDefaults().subscribe {
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
                is SchedulingStartStopResult.Success -> _operationSucceededEvent.value = R.string.auto_tracking_scheduling_successful
                is SchedulingStartStopResult.Failure -> _errorEvent.value = R.string.error_unexpected
            }
        })
    }

    fun cancelAutoTracking() {
        disposable += autoTrackingSchedulingUseCase.cancelAutoTracking().subscribe(Consumer {
            when (it) {
                is CancelAutoTrackingResult.Success -> _operationSucceededEvent.value = R.string.auto_tracking_cancelling_successful
                is CancelAutoTrackingResult.Failure -> _errorEvent.value = R.string.error_cant_stop_auto_tacking
            }
        })
    }

    fun purgeCache() {
        disposable += purgeCacheUseCase.execute().subscribe {
            when (it) {
                is PurgeCacheResult.Progress -> {
                    _loadingEvent.value = true
                }
                is PurgeCacheResult.Success -> {
                    _loadingEvent.value = false
                    _operationSucceededEvent.value = R.string.toast_cache_purged_successfully
                }
                is PurgeCacheResult.Failure -> {
                    _loadingEvent.value = false
                    _errorEvent.value = R.string.error_cant_purge_cache
                }
            }
        }
    }
}