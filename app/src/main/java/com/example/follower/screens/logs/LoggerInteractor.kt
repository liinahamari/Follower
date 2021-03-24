package com.example.follower.screens.logs

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.follower.BuildConfig
import com.example.follower.di.modules.DEBUG_LOGS_DIR
import com.example.follower.di.modules.DEBUG_LOGS_STORAGE_FILE
import com.example.follower.ext.createFileIfNotExist
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Named

/** Refers to <provider>'s authority in AndroidManifest.xml*/
const val FILE_PROVIDER_META = ".fileprovider"
const val ZIPPED_LOGS_FILE_NAME = "logs.zip"

class LoggerInteractor @Inject constructor(
    private val context: Context,
    private val logger: FlightRecorder,
    private val baseComposers: BaseComposers,
    @param:Named(DEBUG_LOGS_STORAGE_FILE) private val logFile: File
) {
    fun getEntireRecord(): Observable<GetRecordResult> = Observable.fromCallable { logger.getEntireRecord() }
        .concatMapIterable { it.split("\n\n".toRegex()).filter { line -> line.isNotBlank() } }
        .map {
            if (it.contains(FlightRecorder.getPriorityPattern(FlightRecorder.Priority.E).toRegex())) {
                val stackTraceLines = "(.*)(label:(.|\n)*)".toRegex().find(it)!!.groupValues[2].split("\n")
                LogUi.ErrorLog(stackTraceLines.first(), stackTraceLines.subList(1, stackTraceLines.size).joinToString(separator = "\n"))
            } else {
                LogUi.InfoLog(it)
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
        .delaySubscription(1, TimeUnit.SECONDS)
        .compose(baseComposers.applyObservableSchedulers())
        .map { if (it) ClearRecordResult.Success else ClearRecordResult.IOError }
        .onErrorReturn { ClearRecordResult.IOError }
        .startWith(ClearRecordResult.InProgress)

    fun createZippedLogsFile(): Single<CreateZipLogsFileResult> = Single.just(BuildConfig.APPLICATION_ID + FILE_PROVIDER_META)
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
        .map<CreateZipLogsFileResult> { CreateZipLogsFileResult.Success(it) }
        .onErrorReturn {
            it.printStackTrace()
            CreateZipLogsFileResult.IOError
        }
        .compose(baseComposers.applySingleSchedulers())

    fun deleteZippedLogs(): Completable = Completable.fromCallable { File(ZIPPED_LOGS_FILE_NAME, DEBUG_LOGS_DIR).delete() }
        .compose(baseComposers.applyCompletableSchedulers())
        .doOnError { logger.e("deteling $ZIPPED_LOGS_FILE_NAME", it) }
}

sealed class CreateZipLogsFileResult {
    data class Success(val path: Uri) : CreateZipLogsFileResult()
    object IOError : CreateZipLogsFileResult()
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