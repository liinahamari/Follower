package com.example.follower.helper

import android.util.Log
import com.example.follower.BuildConfig
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

/** Represents date and in such format: "day_of_month concise_month_name year 24_format_hours:minutes"
 *  For example:
 *  23 Dec 2014 00:12
 *  01 May 2020 05:55
 *  */
private const val DATE_PATTERN_FOR_LOGGING = "dd EEE MMM yyyy HH:mm"

private const val DEBUG_LOGS_DIR = "FlightRecordings"
private const val DEBUG_LOGS_STORAGE_FILE_NAME = "tape.log"

class FlightRecorder(private val logStorage: File) {
    private val isDebug = BuildConfig.DEBUG
    var TAPE_VOLUME = 10 * 1024 * 1024 /** 10 MB **/

    /**
     * @param when - time in milliseconds
     * */
    fun logScheduledEvent(toPrintInLogcat: Boolean = true, what: () -> String, `when`: Long) {
        val message = { what.invoke() + " " + SimpleDateFormat(DATE_PATTERN_FOR_LOGGING, Locale.UK).format(`when`) }
        clearBeginningIfNeeded("} I {", message)
            .also { logStorage.appendText("} I { ${message.invoke()}\n\n") }
            .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, message.invoke())} }
    }

    fun i(toPrintInLogcat: Boolean = true, what: () -> String) = clearBeginningIfNeeded("} I {", what)
        .also { logStorage.appendText("} I { ${what.invoke()}\n\n") }
        .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, what.invoke())} }

    fun d(toPrintInLogcat: Boolean = true, what: () -> String) = clearBeginningIfNeeded("} D {", what)
        .also { logStorage.appendText("} D { ${what.invoke()}\n\n") }
        .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, what.invoke())} }

    fun w(toPrintInLogcat: Boolean = true, what: () -> String) = clearBeginningIfNeeded("} W {", what)
        .also { logStorage.appendText("} W { ${what.invoke()}\n\n") }
        .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, what.invoke())} }

    fun e(toPrintInLogcat: Boolean = true, stackTrace: Array<StackTraceElement>) {
        val readableStackTrace = stackTrace.joinToString(separator = "\n\n") { it.toString() }
        clearBeginningIfNeeded("} E {") { readableStackTrace }
            .also { logStorage.appendText("} E { $readableStackTrace\n\n") }
            .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, readableStackTrace)} }
    }

    fun wtf(toPrintInLogcat: Boolean = true, what: () -> String) = clearBeginningIfNeeded("} X {", what)
        .also { logStorage.appendText("} X { ${what.invoke()}\n\n") }
        .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, what.invoke())} }

    fun getEntireRecord() = try { logStorage.readText() } catch (e: FileNotFoundException) {
        logStorage.createNewFile()
        logStorage.readText()
    }

    fun clear() = logStorage.writeText("")

    private fun clearBeginningIfNeeded(meta: String, what: () -> String) {
        val newDataSize = "$meta ${what.invoke()}\n\n".toByteArray().size
        if ((logStorage.length() + newDataSize.toLong()) > TAPE_VOLUME) {
            val dataToRemain = logStorage.readBytes().drop(newDataSize).toByteArray()
            logStorage.writeBytes(dataToRemain)
        }
    }
}