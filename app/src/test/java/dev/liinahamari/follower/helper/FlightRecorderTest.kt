package dev.liinahamari.follower.helper

import android.os.Build
import android.os.Looper.getMainLooper
import dev.liinahamari.follower.ext.toLogMessage
import dev.liinahamari.follower.ext.yellow
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class FlightRecorderTest {
    private val logFile = createTempFile()
    private val flightRecorder = FlightRecorder(logFile, subscribeOn = Schedulers.trampoline(), observeOn = Schedulers.trampoline())

    @Test
    fun overwriting() {
        shadowOf(getMainLooper()).idle()

        val stringToLog = """
            log_string_01
            log_string_02
            log_string_03
            log_string_04
            log_string_05
            log_string_06
            log_string_07
            log_string_08
            log_string_09
            log_string_10
            log_string_11
            log_string_12
            log_string_13
            log_string_14
            log_string_15
            log_string_16
        """.trimIndent()
        val initialLoadSize = stringToLog
            .toLogMessage(FlightRecorder.Priority.I)
            .toByteArray()
            .size

        println("Initial string consist of $initialLoadSize bytes".yellow())
        println()
        flightRecorder.TAPE_VOLUME = initialLoadSize

        flightRecorder.i { stringToLog }
        assertEquals(initialLoadSize.toLong(), logFile.length())
        println("Initial text in file:".yellow())
        println(logFile.readText())
        println()

        val newLine = "______________________________________________SOME_LARGE_AMOUNT_OF_TEXT____________________________________________"
        flightRecorder.i { newLine }
        assertEquals(initialLoadSize.toLong(), logFile.length())
        println("Text in file after overwriting:".yellow())
        println(logFile.readText())
    }


}