package dev.liinahamari.follower.helper

import android.util.Log
import androidx.annotation.VisibleForTesting
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.ext.now
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.Completable
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

const val SEPARATOR = "/"

class FlightRecorder(private val logStorage: File, private val baseComposers: BaseComposers) {
    private val isDebug = BuildConfig.DEBUG
    var TAPE_VOLUME = 10 * 1024 * 1024  /** 10 MB **/

    companion object {
        fun getPriorityPattern(priority: Priority) = "$SEPARATOR${priority.name}$SEPARATOR"
    }

    private fun String.toLogMessage(priority: Priority) = "${getPriorityPattern(priority)}  ${now()} $SEPARATOR${Thread.currentThread().name}$SEPARATOR: $this\n\n"

    enum class Priority {
        I, D, W, E, WTF
    }

    @VisibleForTesting
    fun printLogAndWriteToFile(logMessage: String, priority: Priority, toPrintInLogcat: Boolean) {
        with(logMessage.toLogMessage(priority)) {
            clearBeginningOfLogFileIfNeeded(this)

            Completable.fromCallable { logStorage.appendText(this) }
                .timeout(5, TimeUnit.SECONDS)
                .compose(baseComposers.applyCompletableSchedulers())
                .subscribe()

            if (toPrintInLogcat && isDebug) {
                Log.i(this::class.java.simpleName, this)
            }
        }
    }

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