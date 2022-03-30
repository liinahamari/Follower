/*
 * Copyright 2020-2022 liinahamari
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

import android.content.Intent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import io.github.kakaocup.kakao.text.KTextView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TITLE_TO_VERIFY = "some_title"
private const val STACK_TRACE_TEXT_TO_VERIFY = "some_stack_trace"

@RunWith(AndroidJUnit4ClassRunner::class)
class CrashStackTraceScreenTest {
    @Suppress("DEPRECATION")
    @get:Rule
    var intentRule = object : IntentsTestRule<CrashStackTraceActivity>(CrashStackTraceActivity::class.java) {
        override fun getActivityIntent(): Intent = CrashStackTraceActivity
            .newIntent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                TITLE_TO_VERIFY,
                STACK_TRACE_TEXT_TO_VERIFY
            )
    }

    @Test
    fun whenArgsPassedToIntent_titleAndStackTraceFilledAccordingly() {
        onScreen<CrashStackTraceScreen> {
            titleView.isVisible()
            titleView.hasText(TITLE_TO_VERIFY)

            errorStackTraceTextView.isVisible()
            errorStackTraceTextView.hasText(STACK_TRACE_TEXT_TO_VERIFY)
        }
    }
}

open class CrashStackTraceScreen : Screen<CrashStackTraceScreen>() {
    val titleView: KTextView = KTextView { withId(R.id.stackTraceTitleTv) }
    val errorStackTraceTextView: KTextView = KTextView { withId(R.id.stacktraceTv) }
}

