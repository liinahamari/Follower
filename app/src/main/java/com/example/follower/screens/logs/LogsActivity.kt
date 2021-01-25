package com.example.follower.screens.logs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.example.follower.FollowerApp
import com.example.follower.R
import com.example.follower.base.BaseActivity
import com.example.follower.ext.DEFAULT_DEBUG_LOG_FILE_NAME
import com.example.follower.ext.errorToast
import com.example.follower.ext.throttleFirst
import com.example.follower.helper.FlightRecorder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_logs.*
import java.io.File
import javax.inject.Inject

private const val MY_EMAIL = "l1bills@protonmail.com"
private const val MESSAGE_TITLE = "Follower Logs"
private const val TEXT_TYPE = "text/plain"

class LogsActivity : BaseActivity(R.layout.activity_logs) {
    private val viewModel by viewModels<LogsActivityViewModel> { viewModelFactory }

    @Inject lateinit var logger: FlightRecorder
    @Inject lateinit var logFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as FollowerApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        logsContainer.text = logger.getEntireRecord() /*todo: async*/
        setupClicks()
        setupViewModelSubscriptions()
    }

    private fun setupViewModelSubscriptions() {
        viewModel.errorEvent.observe(this, { errorToast(getString(it)) })
        viewModel.createFileEvent.observe(this, {
            Intent(Intent.ACTION_SEND).apply {
                type = TEXT_TYPE
                putExtra(Intent.EXTRA_EMAIL, arrayOf(MY_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, MESSAGE_TITLE)
                putExtra(Intent.EXTRA_STREAM, it)
            }.also { intent ->
                startActivity(Intent.createChooser(intent, getString(R.string.title_pick_email_provider)))
            }
        })
    }

    private fun setupClicks() {
        subscriptions += eraseLogButton
            .clicks()
            .throttleFirst()
            .subscribe { logger.clear().also { logsContainer.text = "" } }

        subscriptions += sendLogsButton
            .clicks()
            .throttleFirst()
            .subscribe { requestFileCreation() }
    }

    private fun requestFileCreation() = registerForActivityResult(object : ActivityResultContracts.CreateDocument() {
        override fun createIntent(context: Context, input: String): Intent = super.createIntent(context, input).apply {
            type = TEXT_TYPE
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }) {
        it?.let { viewModel.copyFile(logFile.toUri(), it) }
    }.launch(DEFAULT_DEBUG_LOG_FILE_NAME)
}