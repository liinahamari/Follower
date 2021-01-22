package com.example.follower.screens.logs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

private const val CREATE_DOC_REQUEST_CODE = 100500
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

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) /*todo remove?*/
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CREATE_DOC_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val sourceUri: Uri = logFile.toUri()
                    val targetUri: Uri? = data.data
                    targetUri?.let {
                        viewModel.copyFile(sourceUri, targetUri)
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun requestFileCreation() {
        try { /*TODO: replace with registerForActivityResult()*/
            startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = TEXT_TYPE
                putExtra(Intent.EXTRA_TITLE, DEFAULT_DEBUG_LOG_FILE_NAME)
            }, CREATE_DOC_REQUEST_CODE)
        } catch (e: Exception) {
            errorToast(getString(R.string.title_file_manager_problem))
        }
    }
}