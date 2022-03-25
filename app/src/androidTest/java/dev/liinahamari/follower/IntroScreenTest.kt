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

package dev.liinahamari.follower

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import dev.liinahamari.follower.ext.getDefaultSharedPreferences
import dev.liinahamari.follower.rules.ImmediateSchedulersRule
import dev.liinahamari.follower.screens.intro.IntroActivity
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import io.github.kakaocup.kakao.text.KButton
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*FIXME : problem with run test methods in sequence, @After function seems to be not invoked at all
* Every single @Test function in isolation works well.
* */
@RunWith(AndroidJUnit4ClassRunner::class)
class IntroScreenTest {
    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(IntroActivity::class.java)

    @Rule
    @JvmField
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Before
    fun after() {
        InstrumentationRegistry.getInstrumentation().targetContext.apply {
            getDefaultSharedPreferences().edit().also {
                it.putInt(getString(R.string.pref_app_launch_counter), 0)
            }.commit()
        }
    }

    @Test
    fun onAppStart_introActivityHas4SlidesWithin() {
        onScreen<IntroScreen> {
            doneButton.isInvisible()

            repeat(3) {
                nextButton.click()
            }

            doneButton.isVisible()
        }
    }

    @Test
    fun skipButtonClicked_IntroActivityFinishes() {
        onScreen<IntroScreen> {
            skipButton.isVisible()
            skipButton.click()
            Thread.sleep(2000)
            assert(activityRule.scenario.state == Lifecycle.State.DESTROYED)
        }
    }

    @Test
    fun doneButtonClicked_IntroActivityFinishes() {
        onScreen<IntroScreen> {
            repeat(3) {
                nextButton.click()
            }

            doneButton.isVisible()
            doneButton.click()

            Thread.sleep(2000)
            assert(activityRule.scenario.state == Lifecycle.State.DESTROYED)
        }
    }
}

open class IntroScreen : Screen<IntroScreen>() {
    val nextButton: KButton = KButton { withId(R.id.next) }
    val skipButton: KButton = KButton { withId(R.id.skip) }
    val doneButton: KButton = KButton { withId(R.id.done) }
}