package com.example.follower.screens.address_trace

import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.GetAddressesResult
import com.example.follower.interactors.TrackInteractor
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class AddressTraceViewModel @Inject constructor(private val trackInteractor: TrackInteractor) : BaseViewModel() {
    private val _getAddressesEvent = SingleLiveEvent<List<MapPointer>>()
    val getAddressesEvent: LiveData<List<MapPointer>> get() = _getAddressesEvent

    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

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
