package dev.liinahamari.follower.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel: ViewModel() {
    protected val disposable = CompositeDisposable()
    override fun onCleared() = disposable.clear()

    protected val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent
}
