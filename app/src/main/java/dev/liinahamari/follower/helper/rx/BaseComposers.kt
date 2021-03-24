package dev.liinahamari.follower.helper.rx

import dev.liinahamari.follower.helper.FlightRecorder
import io.reactivex.CompletableTransformer
import io.reactivex.MaybeTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer

class BaseComposers constructor(private val schedulers: SchedulersProvider, private val logger: FlightRecorder) {
    fun <T> applySingleSchedulers(): SingleTransformer<T, T> =
        SingleTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { error -> logger.e(label = "rx", stackTrace = error.stackTrace) }
        }

    fun <T> applyMaybeSchedulers(): MaybeTransformer<T, T> =
        MaybeTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { error -> logger.e(label = "rx", stackTrace = error.stackTrace) }
        }

    fun <T> applyObservableSchedulers(): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { error -> logger.e(label = "rx", stackTrace = error.stackTrace) }
        }

    fun applyCompletableSchedulers(): CompletableTransformer =
        CompletableTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { error -> logger.e(label = "rx", stackTrace = error.stackTrace) }
        }
}