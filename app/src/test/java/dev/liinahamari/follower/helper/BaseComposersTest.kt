package dev.liinahamari.follower.helper

import android.os.Build
import android.os.Looper.getMainLooper
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.helper.rx.TestSchedulers
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
    private val logger = FlightRecorder(logFile, schedulers.io(), schedulers.ui())
    private val baseComposers = BaseComposers(schedulers, logger)

    @Before
    fun setUp() {
        shadowOf(getMainLooper()).idle()
        assert(logger.getEntireRecord().isEmpty())
    }

    @After
    fun tearDown() {
        logFile.writeText("")
        assert(logger.getEntireRecord().isEmpty())
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

        assert(logger.getEntireRecord().isNotBlank())

        with (logger.getEntireRecord().split("\n")) {
            assert(size > 1)
            assert(first().contains(label))
            assert(get(1).contains(exMessage))

            assert(subList(2, size).filter { it.isNotBlank() }.size == exception.stackTrace.size)
            assert(subList(2, size).filter { it.isNotBlank() }[1].trim() == exception.stackTrace[1].toString().trim())
        }
    }
}