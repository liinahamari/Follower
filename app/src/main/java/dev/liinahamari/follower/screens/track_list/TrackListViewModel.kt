package dev.liinahamari.follower.screens.track_list

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.interactors.SharedTrackResult
import dev.liinahamari.follower.interactors.FetchTracksResult
import dev.liinahamari.follower.interactors.RemoveTrackResult
import dev.liinahamari.follower.interactors.TrackInteractor
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.model.TrackDisplayModeResult
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

typealias TrackTitle = String

class TrackListViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val preferencesRepository: PreferencesRepository) : BaseViewModel() {
    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    private val _nonEmptyTrackListEvent = SingleLiveEvent<PagedList<Track>>()
    val nonEmptyTrackListEvent: LiveData<PagedList<Track>> get() = _nonEmptyTrackListEvent

    private val _emptyTrackListEvent = SingleLiveEvent<Any>()
    val emptyTrackListEvent: LiveData<Any> get() = _emptyTrackListEvent

    private val _trackDisplayModeEvent = SingleLiveEvent<Pair<String, Long>>()
    val trackDisplayModeEvent: LiveData<Pair<String, Long>> get() = _trackDisplayModeEvent

    private val _shareJsonEvent = SingleLiveEvent<Pair<Uri, TrackTitle>>()
    val shareJsonEvent: LiveData<Pair<Uri, TrackTitle>> get() = _shareJsonEvent

    private val _removeTrackEvent = SingleLiveEvent<Pair<Int, Boolean>>()
    val removeTrackEvent: LiveData<Pair<Int, Boolean>> get() = _removeTrackEvent

    /*todo: test cascade*/
    fun removeTrack(trackId: Long, position: Int) {
        disposable += trackInteractor.removeTrack(trackId).subscribe(Consumer {
            when (it) {
                is RemoveTrackResult.Success -> _removeTrackEvent.value = position to it.isTracksEmpty
                is RemoveTrackResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
            }
        })
    }

    fun fetchTracks(isTracking: Boolean, trackIdToExclude: Long?) {
        disposable += trackInteractor.fetchTracks(isTracking, trackIdToExclude).subscribe {
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
            when (it) {
                is TrackDisplayModeResult.Success -> _trackDisplayModeEvent.value = it.displayMode to trackId
                is TrackDisplayModeResult.Failure -> _errorEvent.value = R.string.error_unexpected
            }
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
}