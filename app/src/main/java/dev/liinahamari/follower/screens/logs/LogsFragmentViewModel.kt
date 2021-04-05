package dev.liinahamari.follower.screens.logs

import android.net.Uri
import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class LogsFragmentViewModel @Inject constructor(private val loggerInteractor: LoggerInteractor) : BaseViewModel() {
    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    private val _logFilePathEvent = SingleLiveEvent<Uri>()
    val logFilePathEvent: LiveData<Uri> get() = _logFilePathEvent

    private val _emptyLogListEvent = SingleLiveEvent<Any>()
    val emptyLogListEvent: LiveData<Any> get() = _emptyLogListEvent

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _displayLogsEvent = SingleLiveEvent<List<LogUi>>()
    val displayLogsEvent: LiveData<List<LogUi>> get() = _displayLogsEvent

    fun fetchLogs() {
        disposable += loggerInteractor.getEntireRecord().subscribe {
            when (it) {
                is GetRecordResult.Success -> {
                    _displayLogsEvent.value = it.logs
                    _loadingEvent.value = false
                }
                is GetRecordResult.IOError -> {
                    _errorEvent.value = R.string.io_error
                    _loadingEvent.value = false
                }
                is GetRecordResult.EmptyList -> {
                    _emptyLogListEvent.call()
                    _loadingEvent.value = false
                }
                is GetRecordResult.InProgress -> _loadingEvent.value = true
            }
        }
    }

    fun sortLogs(showType: ShowType) {
        disposable += loggerInteractor.sortLogs(showType).subscribe { sortedLogs ->
            when (sortedLogs) {
                is SortResult.Success -> {
                    _displayLogsEvent.value = sortedLogs.logs
                    _loadingEvent.value = false
                }
                is SortResult.Error -> {
                    _errorEvent.value = R.string.io_error
                    _loadingEvent.value = false
                }
                is SortResult.EmptyList -> {
                    _emptyLogListEvent.call()
                    _loadingEvent.value = false
                }
            }
        }
    }

    fun clearLogs() {
        disposable += loggerInteractor.clearEntireRecord().subscribe {
            when (it) {
                is ClearRecordResult.Success -> {
                    _emptyLogListEvent.call()
                    _loadingEvent.value = false
                }
                is ClearRecordResult.IOError -> {
                    _errorEvent.value = R.string.io_error
                    _loadingEvent.value = false
                }
                is ClearRecordResult.InProgress -> _loadingEvent.value = true
            }
        }
    }

    fun createZippedLogsFile() {
        disposable += loggerInteractor.createZippedLogsFile().subscribe {
            when (it) {
                is CreateZipLogsFileResult.InProgress -> _loadingEvent.value = true
                is CreateZipLogsFileResult.Success -> {
                    _loadingEvent.value = false
                    _logFilePathEvent.value = it.path
                }
                is CreateZipLogsFileResult.IOError -> {
                    _loadingEvent.value = false
                    _errorEvent.value = R.string.io_error
                }
            }
        }
    }

    fun deleteZippedLogs() {
        disposable += loggerInteractor.deleteZippedLogs().subscribe()
    }
}