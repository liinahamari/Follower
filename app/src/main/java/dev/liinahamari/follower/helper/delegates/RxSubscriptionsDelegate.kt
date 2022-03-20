/*
 * Copyright 2020-2021 liinahamari
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.liinahamari.follower.helper.delegates

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

interface RxSubscriptionsDelegate {
    fun <T> Observable<T>.subscribeUi(doOnSubscribe: Consumer<T>): Disposable
    fun <T> Observable<T>.subscribeUi(): Disposable

    fun <T> Single<T>.subscribeUi(doOnSubscribe: Consumer<T>): Disposable
    fun <T> Single<T>.subscribeUi(): Disposable

    fun <T> Maybe<T>.subscribeUi(doOnSubscribe: Consumer<T>): Disposable
    fun <T> Maybe<T>.subscribeUi(): Disposable

    fun <T> Flowable<T>.subscribeUi(doOnSubscribe: Consumer<T>): Disposable
    fun <T> Flowable<T>.subscribeUi(): Disposable

    fun Completable.subscribeUi(): Disposable

    fun <T> Observable<T>.addToDisposable(doOnSubscribe: Consumer<T>): Disposable
    fun <T> Observable<T>.addToDisposable(): Disposable

    fun <T> Single<T>.addToDisposable(doOnSubscribe: Consumer<T>): Disposable
    fun <T> Single<T>.addToDisposable(): Disposable

    fun <T> Maybe<T>.addToDisposable(doOnSubscribe: Consumer<T>): Disposable
    fun <T> Maybe<T>.addToDisposable(): Disposable

    fun <T> Flowable<T>.addToDisposable(doOnSubscribe: Consumer<T>): Disposable
    fun <T> Flowable<T>.addToDisposable(): Disposable

    fun Completable.addToDisposable(): Disposable

    /** Must be called manually on lifecycle 'terminate' event */
    fun disposeSubscriptions()
}

class RxSubscriptionDelegateImpl : RxSubscriptionsDelegate {
    private val compositeDisposable = CompositeDisposable()

    override fun <T> Single<T>.subscribeUi(): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
        .also(compositeDisposable::add)

    override fun <T> Maybe<T>.subscribeUi(): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
        .also(compositeDisposable::add)

    override fun <T> Flowable<T>.subscribeUi(): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
        .also(compositeDisposable::add)

    override fun <T> Observable<T>.subscribeUi(): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
        .also(compositeDisposable::add)

    override fun <T> Observable<T>.subscribeUi(doOnSubscribe: Consumer<T>): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(doOnSubscribe)
        .also(compositeDisposable::add)

    override fun <T> Single<T>.subscribeUi(doOnSubscribe: Consumer<T>): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(doOnSubscribe)
        .also(compositeDisposable::add)

    override fun <T> Maybe<T>.subscribeUi(doOnSubscribe: Consumer<T>): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(doOnSubscribe)
        .also(compositeDisposable::add)

    override fun <T> Flowable<T>.subscribeUi(doOnSubscribe: Consumer<T>): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(doOnSubscribe)
        .also(compositeDisposable::add)

    override fun Completable.subscribeUi(): Disposable = subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
        .also(compositeDisposable::add)

    override fun <T> Single<T>.addToDisposable(): Disposable = subscribe()
        .also(compositeDisposable::add)

    override fun <T> Maybe<T>.addToDisposable(): Disposable = subscribe()
        .also(compositeDisposable::add)

    override fun <T> Flowable<T>.addToDisposable(): Disposable = subscribe()
        .also(compositeDisposable::add)

    override fun <T> Observable<T>.addToDisposable(): Disposable = subscribe()
        .also(compositeDisposable::add)

    override fun <T> Observable<T>.addToDisposable(doOnSubscribe: Consumer<T>): Disposable = subscribe(doOnSubscribe)
        .also(compositeDisposable::add)

    override fun <T> Single<T>.addToDisposable(doOnSubscribe: Consumer<T>): Disposable = subscribe(doOnSubscribe)
        .also(compositeDisposable::add)

    override fun <T> Maybe<T>.addToDisposable(doOnSubscribe: Consumer<T>): Disposable = subscribe(doOnSubscribe)
        .also(compositeDisposable::add)

    override fun <T> Flowable<T>.addToDisposable(doOnSubscribe: Consumer<T>): Disposable = subscribe(doOnSubscribe)
        .also(compositeDisposable::add)

    override fun Completable.addToDisposable(): Disposable = subscribe()
        .also(compositeDisposable::add)

    override fun disposeSubscriptions() = compositeDisposable.clear()
}