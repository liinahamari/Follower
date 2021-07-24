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

import android.os.Build
import android.os.Looper.getMainLooper
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.helper.rx.TestSchedulers
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.core.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class BaseComposersTest {
    private val logFile = createTempFile()
    private val schedulers = TestSchedulers()
    private val baseComposers = BaseComposers(schedulers)

    @Before
    fun setUp() {
        shadowOf(getMainLooper()).idle()
        assert(FlightRecorder.getEntireRecord().isEmpty())
    }

    @After
    fun tearDown() {
        logFile.writeText("")
        assert(FlightRecorder.getEntireRecord().isEmpty())
    }

    @Suppress("UNREACHABLE_CODE")
    @Test
    fun `is error happens in some chain, logger must put it's stacktrace to the logging file`() {
        val exMessage = "my_message"
        val exception = IllegalAccessException(exMessage)
        val label = "some_label"

        Single.just(true)
            .map {
                throw exception
                it
            }
            .compose(baseComposers.applySingleSchedulers(label))
            .onErrorResumeWith(Single.just(true))
            .subscribe()

        assert(FlightRecorder.getEntireRecord().isNotBlank())

        with (FlightRecorder.getEntireRecord().split("\n")) {
            assert(size > 1)
            assert(first().contains(label))
            assert(get(1).contains(exMessage))

            assert(subList(2, size).filter { it.isNotBlank() }.size == exception.stackTrace.size)
            assert(subList(2, size).filter { it.isNotBlank() }[1].trim() == exception.stackTrace[1].toString().trim())
        }
    }
}