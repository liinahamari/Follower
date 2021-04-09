package dev.liinahamari.follower.helper.rx

import dev.liinahamari.follower.helper.FlightRecorder
import io.reactivex.CompletableTransformer
import io.reactivex.MaybeTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer

class BaseComposers constructor(private val schedulers: SchedulersProvider, private val logger: FlightRecorder) {
    fun <T> applySingleSchedulers(errorLabel: String = "meta"): SingleTransformer<T, T> =
        SingleTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { err -> logger.e(errorLabel, err) }
        }

    fun <T> applyMaybeSchedulers(errorLabel: String = "meta"): MaybeTransformer<T, T> =
        MaybeTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { err -> logger.e(errorLabel, err) }
        }

    fun <T> applyObservableSchedulers(errorLabel: String = "meta"): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { err -> logger.e(errorLabel, err) }
        }

    fun applyCompletableSchedulers(errorLabel: String = "meta"): CompletableTransformer =
        CompletableTransformer {
            it.subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnError { err -> logger.e(errorLabel, err) }
        }
}