package com.example.follower.interactors

import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LoggerInteractor @Inject constructor(private val logger: FlightRecorder, private val baseComposers: BaseComposers) {
    fun getEntireRecord(): Observable<GetRecordResult> = Observable.fromCallable { logger.getEntireRecord() }
        .compose(baseComposers.applyObservableSchedulers())
        .map<GetRecordResult> { GetRecordResult.Success(it) }
        .onErrorReturn { GetRecordResult.IOError }
        .startWith(GetRecordResult.InProgress)
        .delaySubscription(1, TimeUnit.SECONDS)

    fun clearEntireRecord(): Observable<ClearRecordResult> = Observable.fromCallable { logger.clear() }
        .compose(baseComposers.applyObservableSchedulers())
        .map<ClearRecordResult> { ClearRecordResult.Success }
        .onErrorReturn { ClearRecordResult.IOError }
        .startWith(ClearRecordResult.InProgress)
        .delaySubscription(1, TimeUnit.SECONDS)
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