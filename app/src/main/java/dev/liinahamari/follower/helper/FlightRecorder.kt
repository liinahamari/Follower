package dev.liinahamari.follower.helper

import android.util.Log
import androidx.annotation.VisibleForTesting
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.ext.now
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

const val SEPARATOR = "/"

class FlightRecorder(private val logStorage: File) {
    private val isDebug = BuildConfig.DEBUG

    /** 10 MB **/
    var TAPE_VOLUME = 10 * 1024 * 1024

    companion object {
        fun getPriorityPattern(priority: Priority) = "$SEPARATOR${priority.name}$SEPARATOR"
    }

    private fun String.toLogMessage(priority: Priority) = "${getPriorityPattern(priority)}  ${now()} $SEPARATOR${Thread.currentThread().name}$SEPARATOR: $this\n\n"

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
        toPrintInLogcat: Boolean,
        subscribeOn: Scheduler = Schedulers.io() /*for testing purposes/injecting issue*/,
        observeOn: Scheduler = AndroidSchedulers.mainThread() /*for testing purposes/injecting issue*/
    ) {
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
        val errorMessage = error.stackTrace.joinToString(separator = "\n\t", prefix = "label: $label\n${error.message}\n")
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
        if ((logStorage.length() + newDataSize.toLong()) > TAPE_VOLUME) {
            val dataToRemain = logStorage.readBytes().drop(newDataSize).toByteArray()
            logStorage.writeBytes(dataToRemain)
        }
    }
}