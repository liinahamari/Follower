package com.example.follower.screens.logs

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class LogsActivityViewModel @Inject constructor(private val fileInteractor: FileInteractor) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _createFileEvent = SingleLiveEvent<Uri>()
    val createFileEvent: LiveData<Uri> get() = _createFileEvent

    fun copyFile(originalFileUri: Uri, targetFileUri: Uri) {
        disposable += fileInteractor.copyFile(originalFileUri, targetFileUri).subscribe(Consumer {
            when (it) {
                is FileCreationResult.Success -> _createFileEvent.value = targetFileUri
                is FileCreationResult.IOError -> _errorEvent.value = R.string.io_error
            }
        })
    }
}

sealed class FileCreationResult {
    data class Success(val revealedFileUri: Uri) : FileCreationResult()
    object IOError : FileCreationResult()
}