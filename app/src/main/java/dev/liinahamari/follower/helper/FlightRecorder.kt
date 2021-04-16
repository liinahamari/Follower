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

import android.util.Log
import androidx.annotation.VisibleForTesting
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.ext.toLogMessage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

const val SEPARATOR = "/"

class FlightRecorder(private val logStorage: File,
                     private val subscribeOn: Scheduler = Schedulers.io() /*for testing purposes/injecting issue \ can't be injecting in case of cyclic initialization problem of BaseComposers*/,
                     private val observeOn: Scheduler = AndroidSchedulers.mainThread() /*for testing purposes/injecting issue \ can't be injecting in case of cyclic initialization problem of BaseComposers*/) {
    private val isDebug = BuildConfig.DEBUG

    /** 10 MB **/
    var tapeVolume = 10 * 1024 * 1024

    companion object {
        fun getPriorityPattern(priority: Priority) = "$SEPARATOR${priority.name}$SEPARATOR"
    }

    enum class Priority {
        I,
        D,
        W,
        E,
        WTF,
        L /*lifecycle*/
    }

    @VisibleForTesting
    fun printLogAndWriteToFile(
        logMessage: String,
        priority: Priority,
        toPrintInLogcat: Boolean) {
        with(logMessage.toLogMessage(priority)) {
            clearBeginningOfLogFileIfNeeded(this)

            Completable.fromCallable { logStorage.appendText(this) }
                .timeout(5, TimeUnit.SECONDS)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe()

            if (toPrintInLogcat && isDebug) {
                Log.i(this::class.java.simpleName, this)
            }
        }
    }

    fun lifecycle(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.L, toPrintInLogcat)
    fun i(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.I, toPrintInLogcat)
    fun d(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.D, toPrintInLogcat)
    fun w(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.W, toPrintInLogcat)
    fun wtf(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.WTF, toPrintInLogcat)

    fun e(label: String, error: Throwable, toPrintInLogcat: Boolean = true) {
        val errorMessage = error.stackTrace.joinToString(separator = "\n\t", prefix = "label: $label\n${error.message}\n\t")
        printLogAndWriteToFile(errorMessage, Priority.E, toPrintInLogcat)
        if (toPrintInLogcat) {
            error.printStackTrace()
        }
    }

    fun getEntireRecord() = try {
        logStorage.readText()
    } catch (e: FileNotFoundException) {
        logStorage.createNewFile()
        logStorage.readText()
    }

    private fun clearBeginningOfLogFileIfNeeded(what: String) {
        val newDataSize = what.toByteArray().size
        if ((logStorage.length() + newDataSize.toLong()) > tapeVolume) {
            val dataToRemain = logStorage.readBytes().drop(newDataSize).toByteArray()
            logStorage.writeBytes(dataToRemain)
        }
    }
}