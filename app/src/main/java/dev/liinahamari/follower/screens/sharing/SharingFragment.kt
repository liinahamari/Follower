package dev.liinahamari.follower.screens.sharing

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.activityViewModels
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseDialogFragment
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.helper.CustomToast.infoToast
import dev.liinahamari.follower.helper.CustomToast.successToast
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_sharing.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UploadError
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.ftp.FTPUploadRequest
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

const val ARG_TRACK_FILE_URI = "SharingFragment.ARG_TRACK_FILE_PATH"

class SharingFragment : BaseDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).apply { requestWindowFeature(Window.FEATURE_NO_TITLE) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_sharing, container)

    override fun setupClicks() {
        subscriptions += sendButton.clicks()
            .throttleFirst()
            .subscribe {
                infoToast(getString(R.string.toast_upload_started))

                FTPUploadRequest(requireContext(), serverInputEt.text.toString(), 21)
                    .useSSL(true)
                    .setUsernameAndPassword(loginInputEt.text.toString(), passwordInputEt.text.toString())
                    .addFileToUpload(arguments?.getString(ARG_TRACK_FILE_URI, null)!!, "/home/ftp_user/ftp/files/")
                    .subscribe(requireActivity(), viewLifecycleOwner, delegate = object : RequestObserverDelegate {
                        override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {
                            when (exception) {
                                is UserCancelledUploadException -> errorToast(getString(R.string.error_user_cancelled_upload))
                                is UploadError -> errorToast(String.format(getString(R.string.error_upload_error), exception.serverResponse))
                                else -> errorToast(uploadInfo.toString())
                            }
                            logger.e("Error uploading file to FTP server", exception)
                        }

                        override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) = successToast(getString(R.string.toast_upload_succeed))
                        override fun onCompleted(context: Context, uploadInfo: UploadInfo) = dismiss()
                        override fun onProgress(context: Context, uploadInfo: UploadInfo) = Unit
                        override fun onCompletedWhileNotObserving() = Unit
                    })
            }
        subscriptions += Observable
            .combineLatest(serverInputEt.textChanges().map { it.toString() }, loginInputEt.textChanges().map { it.toString() }, passwordInputEt.textChanges().map { it.toString() },
                { t1: String, t2: String, t3: String -> t1.isNotBlank() && t2.isNotBlank() && t3.isNotBlank() })
            .subscribe {
                sendButton.isEnabled = it
            }
    }
}