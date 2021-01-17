package com.example.follower.screens.track_list

import androidx.lifecycle.LiveData
import com.example.follower.R
import com.example.follower.base.BaseViewModel
import com.example.follower.helper.SingleLiveEvent
import com.example.follower.interactors.FetchTracksResult
import com.example.follower.interactors.RemoveTrackResult
import com.example.follower.interactors.TrackInteractor
import io.reactivex.functions.Consumer
import javax.inject.Inject
import io.reactivex.rxkotlin.plusAssign

class TrackListViewModel @Inject constructor(private val trackInteractor: TrackInteractor) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _fetchAllTracksEvent = SingleLiveEvent<List<TrackUi>>()
    val fetchAllTracksEvent: LiveData<List<TrackUi>> get() = _fetchAllTracksEvent

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

    fun fetchTasks() {
        disposable += trackInteractor.fetchTracks().subscribe(Consumer {
            when (it) {
                is FetchTracksResult.Success -> _fetchAllTracksEvent.value = it.tracks.map { TrackUi(id = it.track.time, title = it.track.title) }
                is FetchTracksResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
            }
        })
    }

}
