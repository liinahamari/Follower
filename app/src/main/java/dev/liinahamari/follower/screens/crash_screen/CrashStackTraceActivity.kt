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
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.follower.R
import dev.liinahamari.follower.databinding.ActivityCrashStackTraceBinding
import dev.liinahamari.follower.ext.argString
import dev.liinahamari.follower.ext.throttleFirst
import dev.liinahamari.follower.ext.withArguments
import dev.liinahamari.follower.helper.delegates.RxSubscriptionDelegateImpl
import dev.liinahamari.follower.helper.delegates.RxSubscriptionsDelegate

class CrashStackTraceActivity :
    AppCompatActivity(R.layout.activity_crash_stack_trace),
    RxSubscriptionsDelegate by RxSubscriptionDelegateImpl() {
    private val ui by viewBinding(ActivityCrashStackTraceBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()
        setupUi()
    }

    override fun onDestroy() {
        disposeSubscriptions()
        super.onDestroy()
    }

    private fun setupUi() {
        ui.stackTraceTitleTv.apply {
            text = argString(EXTRA_TITLE)
            clicks()
                .throttleFirst()
                .addToDisposable { finishAndRemoveTask() }
        }
        ui.stacktraceTv.text = argString(EXTRA_ERROR_STACKTRACE_STRING)
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
            .withArguments(EXTRA_TITLE to title, EXTRA_ERROR_STACKTRACE_STRING to errorStackTrace)
            .apply { flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NO_ANIMATION }
    }
}