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

package dev.liinahamari.follower.screens.crash_screen

import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.liinahamari.follower.R
import dev.liinahamari.follower.databinding.ActivityCrashStackTraceBinding
import dev.liinahamari.follower.ext.getVersionCode

class CrashStackTraceActivity : AppCompatActivity(R.layout.activity_crash_stack_trace) {
    private val ui by viewBinding(ActivityCrashStackTraceBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()
        setupUi()
    }

    private fun setupUi() {
        ui.stackTraceTitleTv.apply {
            text = String.format(getString(R.string.title_app_crashed), intent.getStringExtra(EXTRA_THREAD_NAME)!!, getVersionCode())
            setOnClickListener { finishAndRemoveTask() }
        }
        ui.stacktraceTv.text = (intent.getSerializableExtra(EXTRA_ERROR) as Throwable).stackTraceToString()
    }

    private fun setupWindow() {
        with(window) {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(this@CrashStackTraceActivity, R.color.stacktrace_activity_background)
            navigationBarColor = ContextCompat.getColor(this@CrashStackTraceActivity, R.color.stacktrace_activity_background)
        }
    }

    companion object {
        private const val EXTRA_THREAD_NAME = "CrashStackTraceActivity.EXTRA_THREAD_NAME"
        private const val EXTRA_ERROR = "CrashStackTraceActivity.EXTRA_ERROR"

        fun newIntent(
            context: Context,
            threadName: String,
            throwable: Throwable
        ) = Intent(context, CrashStackTraceActivity::class.java)
            .apply {
                putExtra(EXTRA_THREAD_NAME, threadName)
                putExtra(EXTRA_ERROR, throwable)
                flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NO_ANIMATION
            }
    }
}
