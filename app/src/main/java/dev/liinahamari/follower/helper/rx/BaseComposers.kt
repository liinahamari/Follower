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

package dev.liinahamari.follower.helper.rx

import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaseComposers @Inject constructor() {
    fun <T> applySingleSchedulers(errorLabel: String = "meta"): SingleTransformer<T, T> =
        SingleTransformer {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { err -> FlightRecorder.e(errorLabel, err) }
        }

    fun <T> applyMaybeSchedulers(errorLabel: String = "meta"): MaybeTransformer<T, T> =
        MaybeTransformer {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { err -> FlightRecorder.e(errorLabel, err) }
        }

    fun <T> applyObservableSchedulers(errorLabel: String = "meta"): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { err -> FlightRecorder.e(errorLabel, err) }
        }

    fun applyCompletableSchedulers(errorLabel: String = "meta"): CompletableTransformer =
        CompletableTransformer {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { err -> FlightRecorder.e(errorLabel, err) }
        }

    fun <T> applyFlowableSchedulers(errorLabel: String = "meta"): FlowableTransformer<T, T> =
        FlowableTransformer {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { err -> FlightRecorder.e(errorLabel, err) }
        }
}