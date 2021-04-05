package dev.liinahamari.follower.screens.sharing

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseDialogFragment
import dev.liinahamari.follower.ext.throttleFirst
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_sharing.*
import net.gotev.uploadservice.ftp.FTPUploadRequest

class SharingFragment : BaseDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).apply { requestWindowFeature(Window.FEATURE_NO_TITLE) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_sharing, container)

    private val filePath: BehaviorSubject<String> = BehaviorSubject.create()
    private val filePathSource = Observable.create<String> { a ->
        filePath.subscribe {
            a.onNext(it)
        }
    }
    private val filePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        filePath.onNext(it.toString())
        filePickerBtn.text = it.toString()
    }

    override fun setupClicks() {
        subscriptions += filePickerBtn.clicks()
            .throttleFirst()
            .subscribe { filePicker.launch(arrayOf("*/*")) }
        subscriptions += sendButton.clicks()
            .throttleFirst()
            .subscribe {
                FTPUploadRequest(requireContext(), serverInputEt.text.toString(), 21)
                    .setUsernameAndPassword(loginInputEt.text.toString(), passwordInputEt.text.toString())
                    .addFileToUpload(filePath.value.toString(), "/remote/path")
                    .startUpload()
            }
        subscriptions += Observable
            .combineLatest(serverInputEt.textChanges().map { it.toString() }, loginInputEt.textChanges().map { it.toString() }, passwordInputEt.textChanges().map { it.toString() }, filePathSource,
                { t1: String, t2: String, t3: String, t4: String -> t1.isNotBlank() && t2.isNotBlank() && t3.isNotBlank() && t4.isNotBlank() })
            .subscribe {
                sendButton.isEnabled = it
            }
    }
}