package com.example.follower.interactors

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.follower.BuildConfig
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** relates to <provider>'s authority in AndroidManifest.xml*/
private const val FILE_PROVIDER_META = ".fileprovider"

class LoggerInteractor @Inject constructor(private val context: Context, private val logger: FlightRecorder, private val baseComposers: BaseComposers, private val logFile: File) {
    fun getEntireRecord(): Observable<GetRecordResult> = Observable.fromCallable { logger.getEntireRecord() }
        .delaySubscription(1, TimeUnit.SECONDS)
        .compose(baseComposers.applyObservableSchedulers())
        .map<GetRecordResult> { GetRecordResult.Success(it) }
        .onErrorReturn { GetRecordResult.IOError }
        .startWith(GetRecordResult.InProgress)

    fun clearEntireRecord(): Observable<ClearRecordResult> = Observable.fromCallable { logger.clear() }
        .delaySubscription(1, TimeUnit.SECONDS)
        .compose(baseComposers.applyObservableSchedulers())
        .map<ClearRecordResult> { ClearRecordResult.Success }
        .onErrorReturn { ClearRecordResult.IOError }
        .startWith(ClearRecordResult.InProgress)

    fun getLogFilePath(): Single<GetPathResult> = Single.just(BuildConfig.APPLICATION_ID + FILE_PROVIDER_META)
        .map { FileProvider.getUriForFile(context, it, logFile) }
        .map<GetPathResult> { GetPathResult.Success(it) }
        .onErrorReturn { GetPathResult.IOError }
        .compose(baseComposers.applySingleSchedulers())
}

sealed class GetPathResult {
    data class Success(val path: Uri) : GetPathResult()
    object IOError : GetPathResult()
}

sealed class GetRecordResult {
    object InProgress : GetRecordResult()
    data class Success(val text: String) : GetRecordResult()
    object IOError : GetRecordResult()
}

sealed class ClearRecordResult {
    object InProgress : ClearRecordResult()
    object Success : ClearRecordResult()
    object IOError : ClearRecordResult()
}