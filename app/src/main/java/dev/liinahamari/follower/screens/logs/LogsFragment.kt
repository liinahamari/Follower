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

@file:Suppress("CAST_NEVER_SUCCEEDS")

package dev.liinahamari.follower.screens.logs

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding4.appcompat.navigationClicks
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseFragment
import dev.liinahamari.follower.di.modules.UID
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.helper.CustomToast.errorToast
import dev.liinahamari.follower.helper.CustomToast.successToast
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_logs.*
import javax.inject.Inject
import javax.inject.Named

private const val FILE_SENDING_REQUEST_CODE = 111
const val MY_EMAIL = "l1bills@protonmail.com"
private const val MESSAGE_TITLE = "Follower Logs of "
const val TEXT_TYPE = "text/plain"

/*TODO shall be opened from Settings*/
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

        viewModel.errorEvent.observe(this, { errorToast(it) })

        viewModel.emptyLogListEvent.observe(this, {
            emptyLogsTv.isVisible = true
            logsRv.isVisible = false
            logsAdapter.logs = emptyList()
        })

        viewModel.loadingEvent.observe(this, { toShow ->
            when {
                toShow && loadingDialog.isShowing.not() -> loadingDialog.show()
                toShow.not() && loadingDialog.isShowing -> loadingDialog.cancel()
            }
        })

        viewModel.displayLogsEvent.observe(this, {
            emptyLogsTv.isVisible = false
            logsRv.isVisible = true
            logsAdapter.logs = it
        })

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
                successToast(R.string.sending_logs_successful)
            } else {
                errorToast(R.string.error_sending_logs_unsuccessful)
            }
            viewModel.deleteZippedLogs()
        }
    }

    override fun setupClicks() {
        subscriptions += Observable.combineLatest(
            logsToolbar.menu.findItem(R.id.hideLifecycleEvents)
                .clicks()
                .doOnNext { logsToolbar.menu.findItem(R.id.hideLifecycleEvents).isChecked = logsToolbar.menu.findItem(R.id.hideLifecycleEvents).isChecked.not() }
                .map { logsToolbar.menu.findItem(R.id.hideLifecycleEvents).isChecked }
                .startWithItem(false),
            logsToolbar.menu.findItem(R.id.onlyErrors)
                .clicks()
                .doOnNext { logsToolbar.menu.findItem(R.id.onlyErrors).isChecked = logsToolbar.menu.findItem(R.id.onlyErrors).isChecked.not() }
                .map { logsToolbar.menu.findItem(R.id.onlyErrors).isChecked }
                .startWithItem(false),
            logsToolbar.menu.findItem(R.id.nonMainThreadOnly)
                .clicks()
                .doOnNext { logsToolbar.menu.findItem(R.id.nonMainThreadOnly).isChecked = logsToolbar.menu.findItem(R.id.nonMainThreadOnly).isChecked.not() }
                .map { logsToolbar.menu.findItem(R.id.nonMainThreadOnly).isChecked }
                .startWithItem(false),
            { hideLifecycleEvents: Boolean, onlyErrors: Boolean, nonMainThread: Boolean -> mutableListOf<FilterMode>().apply {
                    if (onlyErrors) {
                        add(FilterMode.SHOW_ERRORS)
                    }
                    if (hideLifecycleEvents) {
                        add(FilterMode.HIDE_LIFECYCLE)
                    }
                    if (nonMainThread) {
                        add(FilterMode.SHOW_NON_MAIN_THREAD)
                    }
            } }
        )
            .skip(1)
            .subscribe {
                viewModel.sortLogs(it)
            }

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