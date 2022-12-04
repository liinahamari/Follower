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

package dev.liinahamari.follower.screens.track_list

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import by.kirich1409.viewbindingdelegate.viewBinding
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseDialogFragment
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.databinding.FragmentSharingBinding
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.helper.CustomToast.infoToast
import dev.liinahamari.follower.helper.CustomToast.successToast
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.helper.delegates.RxSubscriptionDelegateImpl
import dev.liinahamari.follower.helper.delegates.RxSubscriptionsDelegate
import dev.liinahamari.follower.interactors.SharedTrackResult
import dev.liinahamari.follower.interactors.TrackInteractor
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.plusAssign
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UploadError
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.ftp.FTPUploadRequest
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import javax.inject.Inject

const val ARG_TRACK_ID = "SharingFragment.ARG_TRACK_ID"

class FtpSharingFragment : BaseDialogFragment(R.layout.fragment_sharing), RxSubscriptionsDelegate by RxSubscriptionDelegateImpl() {
    private val ui by viewBinding(FragmentSharingBinding::bind)

    private val viewModel by viewModels<FtpSharingViewModel> { viewModelFactory }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).apply { requestWindowFeature(Window.FEATURE_NO_TITLE) }

    override fun setupViewModelSubscriptions() {
        viewModel.shareJsonFtpEvent.observe(viewLifecycleOwner) { jsonUri -> uploadFile(jsonUri) }
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorToast(it) }
    }

    private fun uploadFile(jsonUri: String) {
        FTPUploadRequest(requireContext(), ui.serverInputEt.text.toString(), 21/*todo 40_001?*/)
            .useSSL(true)
            .setUsernameAndPassword(ui.loginInputEt.text.toString(), ui.passwordInputEt.text.toString())
            .addFileToUpload(jsonUri, ui.remotePathEt.text.toString())
            .subscribe(requireActivity(), viewLifecycleOwner, delegate = object : RequestObserverDelegate {
                override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {
                    when (exception) {
                        is UserCancelledUploadException -> errorToast(R.string.error_user_cancelled_upload)
                        is UploadError -> errorToast(String.format(getString(R.string.error_upload_error), exception.serverResponse))
                        else -> errorToast(uploadInfo.toString())
                    }
                    FlightRecorder.e("Error uploading file to FTP server", exception)
                }

                override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
                    successToast(R.string.toast_upload_succeed)
                    dismiss()
                }

                override fun onCompleted(context: Context, uploadInfo: UploadInfo) = Unit
                override fun onProgress(context: Context, uploadInfo: UploadInfo) = Unit
                override fun onCompletedWhileNotObserving() = Unit
            })
    }

    override fun onDestroyView() {
        disposeSubscriptions()
        super.onDestroyView()
    }

    override fun setupClicks() {
        ui.sendButton.clicks()
            .throttleFirst()
            .addToDisposable {
                val trackId = arguments?.getLong(ARG_TRACK_ID, -1L)!!
                viewModel.createSharedJsonFileForTrackFotFtpSharing(trackId)
                infoToast(R.string.toast_upload_started)
            }

        Observable.combineLatest(
            ui.serverInputEt.textChanges().map { it.toString() },
            ui.loginInputEt.textChanges().map { it.toString() },
            ui.passwordInputEt.textChanges().map { it.toString() },
            ui.remotePathEt.textChanges().map { it.toString() }
        ) { t1: String, t2: String, t3: String, t4: String -> t1.isNotBlank() && t2.isNotBlank() && t3.isNotBlank() && t4.isNotBlank() }
            .addToDisposable {
                ui.sendButton.isEnabled = it
            }
    }
}

class FtpSharingViewModel @Inject constructor(private val trackInteractor: TrackInteractor) : BaseViewModel() {
    private val _shareJsonFtpEvent = SingleLiveEvent<String>()
    val shareJsonFtpEvent: LiveData<String> get() = _shareJsonFtpEvent

    fun createSharedJsonFileForTrackFotFtpSharing(trackId: Long) {
        disposable += trackInteractor.getTrackJsonFile(trackId, EXT_JSON).subscribe(Consumer {
            when (it) {
                is SharedTrackResult.Success -> _shareJsonFtpEvent.value = it.trackJsonAndTitle.first.toString()
                is SharedTrackResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
            }
        })
    }
}