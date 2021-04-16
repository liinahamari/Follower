/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.follower.screens.logs

import android.net.Uri
import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

class LogsFragmentViewModel @Inject constructor(private val loggerInteractor: LoggerInteractor) : BaseViewModel() {
    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    private val _logFilePathEvent = SingleLiveEvent<Uri>()
    val logFilePathEvent: LiveData<Uri> get() = _logFilePathEvent

    private val _emptyLogListEvent = SingleLiveEvent<Any>()
    val emptyLogListEvent: LiveData<Any> get() = _emptyLogListEvent

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

    fun sortLogs(filterModes: List<FilterMode>) {
        disposable += loggerInteractor.sortLogs(filterModes).subscribe { sortedLogs ->
            when (sortedLogs) {
                is GetRecordResult.Success -> {
                    _displayLogsEvent.value = sortedLogs.logs
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