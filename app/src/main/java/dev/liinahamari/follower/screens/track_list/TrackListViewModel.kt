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

package dev.liinahamari.follower.screens.track_list

import android.net.Uri
import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.helper.delegates.RxSubscriptionDelegateImpl
import dev.liinahamari.follower.helper.delegates.RxSubscriptionsDelegate
import dev.liinahamari.follower.interactors.*
import dev.liinahamari.follower.model.PreferencesRepository
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

typealias TrackTitle = String

class TrackListViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val preferencesRepository: PreferencesRepository) :
    BaseViewModel(),
    RxSubscriptionsDelegate by RxSubscriptionDelegateImpl() {
    private val _nonEmptyTrackListEvent = SingleLiveEvent<List<TrackUi>>()
    val nonEmptyTrackListEvent: LiveData<List<TrackUi>> get() = _nonEmptyTrackListEvent

    private val _emptyTrackListEvent = SingleLiveEvent<Any>()
    val emptyTrackListEvent: LiveData<Any> get() = _emptyTrackListEvent

    private val _trackDisplayModeEvent = SingleLiveEvent<Pair<String, Long>>()
    val trackDisplayModeEvent: LiveData<Pair<String, Long>> get() = _trackDisplayModeEvent

    private val _shareJsonEvent = SingleLiveEvent<Pair<Uri, TrackTitle>>()
    val shareJsonEvent: LiveData<Pair<Uri, TrackTitle>> get() = _shareJsonEvent

    private val _removeTrackEvent = SingleLiveEvent<Long>()
    val removeTrackEvent: LiveData<Long> get() = _removeTrackEvent

    /*todo: test cascade*/
    fun removeTrack(trackId: Long) {
        disposable += trackInteractor.removeTrack(trackId).subscribe(Consumer {
            when (it) {
                is RemoveTrackResult.Success -> _removeTrackEvent.value = trackId
                is RemoveTrackResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
            }
        })
    }

    override fun onCleared() {
        disposeSubscriptions()
        super.onCleared()
    }

    fun fetchTracks(isTracking: Boolean) {
        disposable += trackInteractor.fetchTracks(isTracking)
            .subscribeUi {
                when (it) {
                    is FetchTracksResult.Success -> _nonEmptyTrackListEvent.value = it.tracks
                    is FetchTracksResult.SuccessEmpty -> _emptyTrackListEvent.call()
                    is FetchTracksResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
                }
            }
    }

    fun saveDisplayType(displayMode: String) {
        disposable += preferencesRepository.saveTrackDisplayMode(displayMode).subscribe()
    }

    fun getTrackDisplayMode(trackId: Long) {
        disposable += preferencesRepository.getTrackDisplayMode().subscribe(Consumer {
            _trackDisplayModeEvent.value = it to trackId
        })
    }

    fun createSharedJsonFileForTrack(trackId: Long, fileExtension: String) {
        disposable += trackInteractor.getTrackJsonFile(trackId, fileExtension).subscribe(Consumer {
            when (it) {
                is SharedTrackResult.Success -> _shareJsonEvent.value = it.trackJsonAndTitle
                is SharedTrackResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
            }
        })
    }

    fun importTracks(uri: Uri, isTracking: Boolean) {
        disposable += trackInteractor.importTracks(uri, isTracking).subscribe(Consumer {
            when (it) {
                is ImportTrackResult.Success -> _nonEmptyTrackListEvent.value = it.tracks
                is ImportTrackResult.ParsingError -> _errorEvent.value = R.string.error_parsing_json
                is ImportTrackResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
                is ImportTrackResult.CommonError -> _errorEvent.value = R.string.error_unexpected
                is ImportTrackResult.EntityAlreadyPresentedError -> _errorEvent.value = R.string.error_track_already_exists
            }
        })
    }
}