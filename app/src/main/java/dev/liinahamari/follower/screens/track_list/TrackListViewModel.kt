package dev.liinahamari.follower.screens.track_list

import android.net.Uri
import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.interactors.*
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.model.TrackDisplayModeResult
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

typealias TrackTitle = String

class TrackListViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val preferencesRepository: PreferencesRepository) : BaseViewModel() {
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

    fun fetchTracks(isTracking: Boolean) {
        disposable += trackInteractor.fetchTracks(isTracking).subscribe(Consumer {
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

    fun importTracks(uri: Uri, isTracking: Boolean) {
        disposable += trackInteractor.importTracks(uri, isTracking).subscribe(Consumer {
            when (it) {
                is ImportTrackResult.Success -> _nonEmptyTrackListEvent.value = it.tracks
                is ImportTrackResult.ParsingError -> _errorEvent.value = R.string.error_parsing_json
                is ImportTrackResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
            }
        })
    }
}