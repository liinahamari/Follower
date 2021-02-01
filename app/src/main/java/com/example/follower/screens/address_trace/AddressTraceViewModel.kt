package com.example.follower.screens.address_trace

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.GetAddressesResult
import com.example.follower.interactors.TrackInteractor
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class AddressTraceViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val context: Context) : BaseViewModel() {
    private val _getTrackEvent = SingleLiveEvent<List<String>>()
    val getTrackEvent: LiveData<List<String>> get() = _getTrackEvent

    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    fun getAddressTrace(id: Long) {
        disposable += trackInteractor.getAddressesList(id)
            .subscribe {
                when (it) {
                    is GetAddressesResult.Success -> {
                        _loadingEvent.value = false
                        _getTrackEvent.value = it.addresses
                    }
                    is GetAddressesResult.DatabaseCorruptionError -> {
                        _loadingEvent.value = false
                        _errorEvent.value = context.getString(R.string.db_error)
                    }
                    is GetAddressesResult.Loading -> _loadingEvent.value = true
                }
            }
    }
}
