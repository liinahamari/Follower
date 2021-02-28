package com.example.follower.screens.logs

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.ClearRecordResult
import com.example.follower.interactors.GetPathResult
import com.example.follower.interactors.GetRecordResult
import com.example.follower.interactors.LoggerInteractor
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class LogsFragmentViewModel @Inject constructor(private val loggerInteractor: LoggerInteractor) : BaseViewModel() {
    private val _clearLogsEvent = SingleLiveEvent<Any>()
    val clearLogsEvent: LiveData<Any> get() = _clearLogsEvent

    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    private val _logFilePathEvent = SingleLiveEvent<Uri>()
    val logFilePathEvent: LiveData<Uri> get() = _logFilePathEvent

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
                is GetRecordResult.InProgress -> _loadingEvent.value = true
            }
        }
    }

    fun clearLogs() {
        disposable += loggerInteractor.clearEntireRecord().subscribe {
            when (it) {
                is ClearRecordResult.Success -> {
                    _clearLogsEvent.call()
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

    fun requestLogFilePath() {
        disposable += loggerInteractor.getLogFilePath().subscribe(Consumer {
            when (it) {
                is GetPathResult.Success -> _logFilePathEvent.value = it.path
                is GetPathResult.IOError -> _errorEvent.value = R.string.io_error
            }
        })
    }
}