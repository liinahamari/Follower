@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.example.follower.screens.logs

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.di.modules.UID
import com.example.follower.ext.throttleFirst
import com.example.follower.helper.CustomToast.errorToast
import com.example.follower.helper.CustomToast.successToast
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_logs.*
import javax.inject.Inject
import javax.inject.Named

private const val FILE_SENDING_REQUEST_CODE = 111
const val MY_EMAIL = "l1bills@protonmail.com"
private const val MESSAGE_TITLE = "Follower Logs of "
const val TEXT_TYPE = "text/plain"

class LogsFragment : BaseFragment(R.layout.fragment_logs) {
    @Inject
    @Named(UID)
    lateinit var userId: String

    private val loadingDialog by lazy {
        Dialog(requireActivity(), R.style.DialogNoPaddingNoTitle).apply {
            setContentView(R.layout.dialog_saving)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    private val viewModel by viewModels<LogsFragmentViewModel> { viewModelFactory }
    private val logsAdapter = LogsAdapter()

    override fun onAttach(context: Context) {
        (context.applicationContext as FollowerApp).appComponent.inject(this)
        super.onAttach(context)
        viewModel.fetchLogs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logsRv.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = logsAdapter
        }
    }

    override fun setupViewModelSubscriptions() {
        super.setupViewModelSubscriptions()

        viewModel.errorEvent.observe(this, { errorToast(getString(it)) })

        viewModel.loadingEvent.observe(this, { toShow ->
            when {
                toShow && loadingDialog.isShowing.not() -> loadingDialog.show()
                toShow.not() && loadingDialog.isShowing -> loadingDialog.cancel()
            }
        })

        viewModel.displayLogsEvent.observe(this, { logsAdapter.logs = it })

        viewModel.clearLogsEvent.observe(this, { logsAdapter.logs = emptyList() })

        viewModel.logFilePathEvent.observe(this, {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(MY_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, MESSAGE_TITLE + userId)
                putExtra(Intent.EXTRA_STREAM, it)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = TEXT_TYPE
            }.also {
                @Suppress("DEPRECATION")
                startActivityForResult(it, FILE_SENDING_REQUEST_CODE)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION") super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SENDING_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                successToast(getString(R.string.sending_logs_successful))
            } else {
                errorToast(getString(R.string.error_sending_logs_unsuccessful))
            }
            viewModel.deleteZippedLogs()
        }
    }

    override fun setupClicks() {
        subscriptions += logsToolbar.menu.findItem(R.id.onlyErrors)
            .clicks()
            .throttleFirst()
            .map {
                logsToolbar.menu.findItem(R.id.onlyErrors).isChecked = logsToolbar.menu.findItem(R.id.onlyErrors).isChecked.not()
                logsToolbar.menu.findItem(R.id.onlyErrors).isChecked
            }
            .subscribe { logsAdapter.sort(it) }

        subscriptions += logsToolbar.menu.findItem(R.id.sendLogs)
            .clicks()
            .throttleFirst()
            .subscribe { viewModel.createZippedLogsFile() }

        subscriptions += logsToolbar.menu.findItem(R.id.clearLogs)
            .clicks()
            .throttleFirst()
            .subscribe { viewModel.clearLogs() }

        subscriptions += logsToolbar
            .navigationClicks()
            .throttleFirst()
            .subscribe { Navigation.findNavController(requireView()).popBackStack() }
    }
}