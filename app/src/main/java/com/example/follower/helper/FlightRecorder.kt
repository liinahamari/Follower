package com.example.follower.helper

import android.util.Log
import com.example.follower.BuildConfig
import com.example.follower.ext.today
import java.io.File
import java.io.FileNotFoundException

class FlightRecorder(private val logStorage: File) {
    private val isDebug = BuildConfig.DEBUG
    var TAPE_VOLUME = 10 * 1024 * 1024

    /** 10 MB **/

    fun i(toPrintInLogcat: Boolean = true, what: () -> String) {
        clearBeginningIfNeeded("} I {", what, today())
        logStorage.appendText("} I { ${today()} ${what.invoke()}\n\n")
        if (toPrintInLogcat && isDebug) {
            Log.i(this::class.java.simpleName, what.invoke())
        }
    }

    fun d(toPrintInLogcat: Boolean = true, what: () -> String) {
        clearBeginningIfNeeded("} D {", what, today())
        logStorage.appendText("} D { ${today()} ${what.invoke()}\n\n")
        if (toPrintInLogcat && isDebug) {
            Log.i(this::class.java.simpleName, what.invoke())
        }
    }

    fun w(toPrintInLogcat: Boolean = true, what: () -> String) {
        clearBeginningIfNeeded("} W {", what, today())
        logStorage.appendText("} W { ${today()} ${what.invoke()}\n\n")
        if (toPrintInLogcat && isDebug) {
            Log.i(this::class.java.simpleName, what.invoke())
        }
    }

    /*TODO ability for LogActivity to view expandable stack traces*/
    /*TODO improve tests*/
    fun e(label: String, toPrintInLogcat: Boolean = true, stackTrace: Array<StackTraceElement>) {
        val readableStackTrace = stackTrace.joinToString(separator = "\n") { it.toString() }
        clearBeginningIfNeeded("} E {", { "$readableStackTrace label: $label" }, today())
        logStorage.appendText("} E { ${today()} label: $label\n")
        logStorage.appendText("} E { $readableStackTrace\n\n")
        if (toPrintInLogcat && isDebug) {
            Log.w(this::class.java.simpleName, readableStackTrace)
        }
    }

    fun wtf(toPrintInLogcat: Boolean = true, what: () -> String) {
        clearBeginningIfNeeded("} X {", what, today())
        logStorage.appendText("} X { ${today()} ${what.invoke()}\n\n")
        if (toPrintInLogcat && isDebug) {
            Log.i(this::class.java.simpleName, what.invoke())
        }
    }

    fun getEntireRecord() = try {
        logStorage.readText()
    } catch (e: FileNotFoundException) {
        logStorage.createNewFile()
        logStorage.readText()
    }

    fun clear(): Boolean = kotlin.runCatching { logStorage.writeText("") }.isSuccess

    private fun clearBeginningIfNeeded(meta: String, what: () -> String, timestamp: String) {
        val newDataSize = "$meta $timestamp ${what.invoke()}\n\n".toByteArray().size
        if ((logStorage.length() + newDataSize.toLong()) > TAPE_VOLUME) {
            val dataToRemain = logStorage.readBytes().drop(newDataSize).toByteArray()
            logStorage.writeBytes(dataToRemain)
        }
    }
}