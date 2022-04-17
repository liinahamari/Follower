/*
 * Copyright 2020-2021 liinahamari
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.liinahamari.feature.crash_screen.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class CrashStackTraceActivity : AppCompatActivity(R.layout.activity_crash_stack_trace) {
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()
        setupUi()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
    }

    private fun setupUi() {
        findViewById<TextView>(R.id.stackTraceTitleTv).apply {
            text = intent.getStringExtra(EXTRA_TITLE)
            setOnClickListener { finishAndRemoveTask() }
        }
        findViewById<TextView>(R.id.stacktraceTv).apply {
            text = intent.getStringExtra(EXTRA_ERROR_STACKTRACE_STRING)
            setOnClickListener { finishAndRemoveTask() }
        }
    }

    private fun setupWindow() {
        with(window) {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(this@CrashStackTraceActivity, R.color.stacktrace_activity_background)
            navigationBarColor = ContextCompat.getColor(this@CrashStackTraceActivity, R.color.stacktrace_activity_background)
        }
    }

    companion object {
        private const val EXTRA_TITLE = "CrashStackTraceActivity.EXTRA_TITLE"
        private const val EXTRA_ERROR_STACKTRACE_STRING = "CrashStackTraceActivity.EXTRA_ERROR_STACKTRACE_STRING"

        fun newIntent(
            context: Context,
            title: String,
            errorStackTrace: String
        ) = Intent(context, CrashStackTraceActivity::class.java)
            .also {
                it.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NO_ANIMATION

                it.putExtra(EXTRA_TITLE, title)
                it.putExtra(EXTRA_ERROR_STACKTRACE_STRING, errorStackTrace)
            }
    }
}