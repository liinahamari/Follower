package dev.liinahamari.follower.screens.track_list

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseDialogFragment
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.helper.CustomToast.infoToast
import dev.liinahamari.follower.helper.CustomToast.successToast
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.interactors.SharedTrackResult
import dev.liinahamari.follower.interactors.TrackInteractor
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_sharing.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UploadError
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.ftp.FTPUploadRequest
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import javax.inject.Inject

const val ARG_TRACK_ID = "SharingFragment.ARG_TRACK_ID"

class FtpSharingFragment : BaseDialogFragment() {
    private val viewModel by viewModels<FtpSharingViewModel> { viewModelFactory }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).apply { requestWindowFeature(Window.FEATURE_NO_TITLE) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_sharing, container)

    override fun setupViewModelSubscriptions() {
        viewModel.shareJsonFtpEvent.observe(viewLifecycleOwner) { jsonUri -> uploadFile(jsonUri) }
        viewModel.errorEvent.observe(viewLifecycleOwner, { errorToast(it) })
    }

    private fun uploadFile(jsonUri: String) {
        FTPUploadRequest(requireContext(), serverInputEt.text.toString(), 21/*todo 40_001?*/)
            .useSSL(true)
            .setUsernameAndPassword(loginInputEt.text.toString(), passwordInputEt.text.toString())
            .addFileToUpload(jsonUri, remotePathEt.text.toString())
            .subscribe(requireActivity(), viewLifecycleOwner, delegate = object : RequestObserverDelegate {
                override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {
                    when (exception) {
                        is UserCancelledUploadException -> errorToast(R.string.error_user_cancelled_upload)
                        is UploadError -> errorToast(String.format(getString(R.string.error_upload_error), exception.serverResponse))
                        else -> errorToast(uploadInfo.toString())
                    }
                    logger.e("Error uploading file to FTP server", exception)
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

    override fun setupClicks() {
        if (BuildConfig.DEBUG) {
            serverInputEt.setText(BuildConfig.TEST_FTP_SERVER_ADDRESS)
            loginInputEt.setText(BuildConfig.TEST_FTP_SERVER_USER_LOGIN)
            passwordInputEt.setText(BuildConfig.TEST_FTP_SERVER_USER_PASSWORD)
            remotePathEt.setText(BuildConfig.TEST_FTP_SERVER_REMOTE_PATH)
        }

        subscriptions += sendButton.clicks()
            .throttleFirst()
            .subscribe {
                val trackId = arguments?.getLong(ARG_TRACK_ID, -1L)!!
                viewModel.createSharedJsonFileForTrackFotFtpSharing(trackId)
                infoToast(R.string.toast_upload_started)
            }
        subscriptions += Observable
            .combineLatest(serverInputEt.textChanges().map { it.toString() }, loginInputEt.textChanges().map { it.toString() }, passwordInputEt.textChanges().map { it.toString() }, remotePathEt.textChanges().map { it.toString() },
                { t1: String, t2: String, t3: String , t4: String -> t1.isNotBlank() && t2.isNotBlank() && t3.isNotBlank() && t4.isNotBlank() })
            .subscribe {
                sendButton.isEnabled = it
            }
    }
}

class FtpSharingViewModel @Inject constructor(private val trackInteractor: TrackInteractor): BaseViewModel() {
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