@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.example.follower.screens.logs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseFragment
import com.example.follower.di.modules.UID
import com.example.follower.ext.throttleFirst
import com.example.follower.helper.CustomToast.errorToast
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_logs.*
import javax.inject.Inject
import javax.inject.Named

private const val MY_EMAIL = "l1bills@protonmail.com"
private const val MESSAGE_TITLE = "Follower Logs of "
const val TEXT_TYPE = "text/plain"

class LogsFragment : BaseFragment(R.layout.fragment_logs) {
    @Inject
    @Named(UID)
    lateinit var userId: String

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

        viewModel.loadingEvent.observe(this, { progressBar.isVisible = it })

        viewModel.displayLogsEvent.observe(this, { logsAdapter.logs = it })

        viewModel.clearLogsEvent.observe(this, { logsAdapter.logs = emptyList() })

        viewModel.logFilePathEvent.observe(this, {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(MY_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, MESSAGE_TITLE + userId)
                putExtra(Intent.EXTRA_STREAM, it)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = TEXT_TYPE
            }.also { startActivity(it) }
        })
    }

    override fun setupClicks() {
        subscriptions += logsToolbar.menu.findItem(R.id.sendLogs)
            .clicks()
            .throttleFirst()
            .subscribe { viewModel.requestLogFilePath() }

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