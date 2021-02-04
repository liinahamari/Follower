package com.example.follower.screens.track_list

import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.FetchTracksResult
import com.example.follower.interactors.RemoveTrackResult
import com.example.follower.interactors.TrackInteractor
import com.example.follower.model.PreferencesRepository
import com.example.follower.model.TrackDisplayModeResult
import io.reactivex.functions.Consumer
import javax.inject.Inject
import io.reactivex.rxkotlin.plusAssign

class TrackListViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val preferencesRepository: PreferencesRepository) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _nonEmptyTrackListEvent = SingleLiveEvent<List<TrackUi>>()
    val nonEmptyTrackListEvent: LiveData<List<TrackUi>> get() = _nonEmptyTrackListEvent

    private val _emptyTrackListEvent = SingleLiveEvent<Any>()
    val emptyTrackListEvent: LiveData<Any> get() = _emptyTrackListEvent

    private val _trackDisplayModeEvent = SingleLiveEvent<Pair<String, Long>>()
    val trackDisplayModeEvent: LiveData<Pair<String, Long>> get() = _trackDisplayModeEvent

    private val _removeTrackEvent = SingleLiveEvent<Long>()
    val removeTrackEvent: LiveData<Long> get() = _removeTrackEvent

    fun removeTask(taskId: Long) {
        disposable += trackInteractor.removeTrack(taskId).subscribe(Consumer {
            when (it) {
                is RemoveTrackResult.Success -> _removeTrackEvent.value = taskId
                is RemoveTrackResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
            }
        })
    }

    fun fetchTracks() {
        disposable += trackInteractor.fetchTracks().subscribe(Consumer {
            when (it) {
                is FetchTracksResult.Success -> _nonEmptyTrackListEvent.value = it.tracks
                is FetchTracksResult.SuccessEmpty -> _emptyTrackListEvent.call()
                is FetchTracksResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
            }
        })
    }

    fun saveDisplayType(displayMode: String) {
        disposable += preferencesRepository.saveTrackDisplayMode(displayMode).subscribe()
    }

    fun getTrackDisplayMode(trackId: Long) {
        disposable += preferencesRepository.getTrackDisplayMode().subscribe(Consumer {
            when (it) {
                is TrackDisplayModeResult.Success -> _trackDisplayModeEvent.value = it.displayMode to trackId
                is TrackDisplayModeResult.Failure -> _errorEvent.value = R.string.error_couldnt_save_preference
            }
        })
    }
}
