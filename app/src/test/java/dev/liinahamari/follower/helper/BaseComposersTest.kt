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

package dev.liinahamari.follower.helper

import android.app.Instrumentation
import android.os.Build
import android.os.Looper.getMainLooper
import androidx.test.platform.app.InstrumentationRegistry
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.ImmediateSchedulersRule
import dev.liinahamari.loggy_sdk.Loggy
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class BaseComposersTest {
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    private val testee = BaseComposers()

    @Before
    fun init() {
        Loggy.initForTest(Instrumentation.newApplication(FollowerApp::class.java, InstrumentationRegistry.getInstrumentation().context))
    }
    
    @Test
    fun `if error happens in a chain using applySingleSchedulers, it will be logged by FlightRecorder (and printed to logcat)`() {
        shadowOf(getMainLooper()).idle()

        mockkObject(FlightRecorder)

        Single.just(1)
            .map {
                throw IllegalArgumentException()
                it
            }
            .compose(testee.applySingleSchedulers())
            .test()
            .assertError(IllegalArgumentException::class.java)

        verify(exactly = 1) { FlightRecorder.e(any(), any()) }
    }

    @Test
    fun `if error happens in a chain using applyObservableSchedulers, it will be logged by FlightRecorder (and printed to logcat)`() {
        shadowOf(getMainLooper()).idle()

        mockkObject(FlightRecorder)

        Observable.just(1)
            .map {
                throw IllegalArgumentException()
                it
            }
            .compose(testee.applyObservableSchedulers())
            .test()
            .assertError(IllegalArgumentException::class.java)

        verify(exactly = 1) { FlightRecorder.e(any(), any()) }
    }

    @Test
    fun `if error happens in a chain using applyMaybeSchedulers, it will be logged by FlightRecorder (and printed to logcat)`() {
        shadowOf(getMainLooper()).idle()

        mockkObject(FlightRecorder)

        Maybe.just(1)
            .map {
                throw IllegalArgumentException()
                it
            }
            .compose(testee.applyMaybeSchedulers())
            .test()
            .assertError(IllegalArgumentException::class.java)

        verify(exactly = 1) { FlightRecorder.e(any(), any()) }
    }

    @Suppress("UNREACHABLE_CODE")
    @Test
    fun `if error happens in a chain using applyFlowableSchedulers, it will be logged by FlightRecorder (and printed to logcat)`() {
        shadowOf(getMainLooper()).idle()

        mockkObject(FlightRecorder)

        Flowable.just(1)
            .map {
                throw IllegalArgumentException()
                it
            }
            .compose(testee.applyFlowableSchedulers())
            .test()
            .assertError(IllegalArgumentException::class.java)

        verify(exactly = 1) { FlightRecorder.e(any(), any()) }
    }

    @Suppress("UNREACHABLE_CODE")
    @Test
    fun `if error happens in a chain using applyCompletableSchedulers, it will be logged by FlightRecorder (and printed to logcat)`() {
        shadowOf(getMainLooper()).idle()

        mockkObject(FlightRecorder)

        Single.just(1)
            .map {
                throw IllegalArgumentException()
                it
            }
            .ignoreElement()
            .compose(testee.applyCompletableSchedulers())
            .test()
            .assertError(IllegalArgumentException::class.java)

        verify(exactly = 1) { FlightRecorder.e(any(), any()) }
    }
}