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

package dev.liinahamari.follower.screens.address_trace

import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.interactors.GetAddressesResult
import dev.liinahamari.follower.interactors.TrackInteractor
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

class AddressTraceViewModel @Inject constructor(private val trackInteractor: TrackInteractor) : BaseViewModel() {
    private val _getAddressesEvent = SingleLiveEvent<List<MapPointer>>()
    val getAddressesEvent: LiveData<List<MapPointer>> get() = _getAddressesEvent

    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    fun getAddressTrace(id: Long) {
        disposable += trackInteractor.getAddressesList(id)
            .subscribe {
                when (it) {
                    is GetAddressesResult.Success -> {
                        _loadingEvent.value = false
                        _getAddressesEvent.value = it.addresses
                    }
                    is GetAddressesResult.DatabaseCorruptionError -> {
                        _loadingEvent.value = false
                        _errorEvent.value = R.string.db_error
                    }
                    is GetAddressesResult.Loading -> _loadingEvent.value = true
                }
            }
    }
}
