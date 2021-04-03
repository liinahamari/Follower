package com.example.follower.screens.logs

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.follower.BuildConfig
import com.example.follower.di.modules.APP_CONTEXT
import com.example.follower.di.modules.DEBUG_LOGS_DIR
import com.example.follower.di.modules.DEBUG_LOGS_STORAGE_FILE
import com.example.follower.ext.DATE_PATTERN_FOR_LOGGING
import com.example.follower.ext.createFileIfNotExist
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.SEPARATOR
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Completable
import io.reactivex.Observable
import java.io.*
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Named

/** Refers to <provider>'s authority in AndroidManifest.xml*/
const val FILE_PROVIDER_META = ".fileprovider"
const val ZIPPED_LOGS_FILE_NAME = "logs.zip"

/** Be sure what it is matching pattern in use of FlightRecorder class*/
private val LOG_PATTERN_REGEX = "${SEPARATOR}(\\w)${SEPARATOR}\\s+(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}:\\d{3})\\s${SEPARATOR}(\\w+)${SEPARATOR}:\\s+((.|\n|\t)*)".toRegex()

class LoggerInteractor @Inject constructor(
    @Named(APP_CONTEXT) private val context: Context,
    private val logger: FlightRecorder,
    private val baseComposers: BaseComposers,
    @param:Named(DEBUG_LOGS_STORAGE_FILE) private val logFile: File
) {
    fun getEntireRecord(): Observable<GetRecordResult> = Observable.fromCallable { logger.getEntireRecord() }
        .concatMapIterable { it.split("\n\n".toRegex()).filter { line -> line.isNotBlank() } }
        .map {
            val (priority, time, thread, logMessage) = LOG_PATTERN_REGEX.find(it)!!.groupValues.drop(1)
            if (priority == FlightRecorder.Priority.E.name) {
                val stackTraceLines = "(.*)(label:(.|\n)*)".toRegex().find(logMessage)!!.groupValues[2].split("\n")
                LogUi.ErrorLog(
                    label = stackTraceLines.first(),
                    stacktrace = stackTraceLines.subList(1, stackTraceLines.size).joinToString(separator = "\n"),
                    time = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING).parse(time)!!.time,
                    thread = thread
                )
            } else {
                LogUi.InfoLog(message = it,
                    time = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING).parse(time)!!.time,
                    thread = thread
                )
            }
        }
        .toList()
        .toObservable()
        .delaySubscription(750, TimeUnit.MILLISECONDS)
        .compose(baseComposers.applyObservableSchedulers())
        .map<GetRecordResult> { GetRecordResult.Success(it) }
        .onErrorReturn { GetRecordResult.IOError }
        .startWith(GetRecordResult.InProgress)

    fun clearEntireRecord(): Observable<ClearRecordResult> = Observable.fromCallable {
        kotlin.runCatching { logFile.writeText("") }.isSuccess
    }
        .delaySubscription(750, TimeUnit.MILLISECONDS)
        .compose(baseComposers.applyObservableSchedulers())
        .map { if (it) ClearRecordResult.Success else ClearRecordResult.IOError }
        .onErrorReturn { ClearRecordResult.IOError }
        .startWith(ClearRecordResult.InProgress)

    fun createZippedLogsFile(): Observable<CreateZipLogsFileResult> = Observable.just(BuildConfig.APPLICATION_ID + FILE_PROVIDER_META)
        .map { authority ->
            val zippedLogs = context.createFileIfNotExist(ZIPPED_LOGS_FILE_NAME, DEBUG_LOGS_DIR)
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zippedLogs))).use { output ->
                val data = ByteArray(1024)
                BufferedInputStream(FileInputStream(logFile), data.size).use { input ->
                    output.putNextEntry(ZipEntry(logFile.name))
                    var counter: Int
                    while (input.read(data, 0, data.size).also { counter = it } != -1) {
                        output.write(data, 0, counter)
                    }
                }
            }
            FileProvider.getUriForFile(context, authority, zippedLogs)
        }
        .delaySubscription(750, TimeUnit.MILLISECONDS)
        .compose(baseComposers.applyObservableSchedulers())
        .map<CreateZipLogsFileResult> { CreateZipLogsFileResult.Success(it) }
        .onErrorReturn {
            it.printStackTrace()
            CreateZipLogsFileResult.IOError
        }
        .startWith(CreateZipLogsFileResult.InProgress)

    fun deleteZippedLogs(): Completable = Completable.fromCallable { File(ZIPPED_LOGS_FILE_NAME, DEBUG_LOGS_DIR).delete() }
        .compose(baseComposers.applyCompletableSchedulers())
        .doOnError { logger.e("deteling $ZIPPED_LOGS_FILE_NAME", it) }
}

sealed class CreateZipLogsFileResult {
    data class Success(val path: Uri) : CreateZipLogsFileResult()
    object IOError : CreateZipLogsFileResult()
    object InProgress : CreateZipLogsFileResult()
}

sealed class GetRecordResult {
    object InProgress : GetRecordResult()
    data class Success(val logs: List<LogUi>) : GetRecordResult()
    object IOError : GetRecordResult()
}

sealed class ClearRecordResult {
    object InProgress : ClearRecordResult()
    object Success : ClearRecordResult()
    object IOError : ClearRecordResult()
}