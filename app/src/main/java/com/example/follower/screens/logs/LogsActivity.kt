@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.example.follower.screens.logs

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseActivity
import com.example.follower.ext.errorToast
import com.example.follower.ext.throttleFirst
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_logs.*

private const val MY_EMAIL = "l1bills@protonmail.com"
private const val MESSAGE_TITLE = "Follower Logs"
private const val TEXT_TYPE = "text/plain"

class LogsActivity : BaseActivity(R.layout.activity_logs) {
    private val viewModel by viewModels<LogsActivityViewModel> { viewModelFactory }
    private val logsAdapter = LogsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        logsRv.apply {
            layoutManager = LinearLayoutManager(this@LogsActivity)
            adapter = logsAdapter
        }
    }

    override fun onResume() = super.onResume().also { viewModel.fetchLogs() }

    override fun setupViewModelSubscriptions() {
        super.setupViewModelSubscriptions()

        viewModel.errorEvent.observe(this, { errorToast(getString(it)) })

        viewModel.loadingEvent.observe(this, { progressBar.isVisible = it })

        viewModel.displayLogsEvent.observe(this, { logsAdapter.logs = it })

        viewModel.clearLogsEvent.observe(this, { logsAdapter.logs = emptyList() })

        viewModel.logFilePathEvent.observe(this, {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(MY_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, MESSAGE_TITLE)
                putExtra(Intent.EXTRA_STREAM, it)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = TEXT_TYPE
            }.also { startActivity(it) }
        })
    }

    override fun setupClicks() {
        subscriptions += eraseLogButton
            .clicks()
            .throttleFirst()
            .subscribe { viewModel.clearLogs() }

        subscriptions += sendLogsButton
            .clicks()
            .throttleFirst()
            .subscribe { viewModel.requestLogFilePath() }
    }
}