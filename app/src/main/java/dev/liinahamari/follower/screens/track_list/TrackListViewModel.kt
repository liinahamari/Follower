package dev.liinahamari.follower.screens.track_list

import android.net.Uri
import androidx.lifecycle.LiveData
import dev.liinahamari.follower.R
import dev.liinahamari.follower.base.BaseViewModel
import dev.liinahamari.follower.helper.SingleLiveEvent
import dev.liinahamari.follower.interactors.*
import dev.liinahamari.follower.model.PreferenceRepository
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

typealias TrackTitle = String

@ExperimentalCoroutinesApi
class TrackListViewModel @Inject constructor(private val trackInteractor: TrackInteractor, private val preferencesRepository: PreferenceRepository) : BaseViewModel() {
    private val _isDarkThemeEnabledEvent = SingleLiveEvent<Boolean>()
    val isDarkThemeEnabledEvent: LiveData<Boolean> get() = _isDarkThemeEnabledEvent

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

    fun saveDisplayType(displayMode: String) = preferencesRepository.updateTrackRepresentation(displayMode)

    fun getTrackDisplayMode(trackId: Long) {
        disposable += preferencesRepository.trackRepresentation.subscribe {
            _trackDisplayModeEvent.value = it to trackId
        }
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

    fun isDarkModeEnabled() {
        disposable += preferencesRepository.isDarkThemeEnabled.subscribe {
            _isDarkThemeEnabledEvent.value = it
        }
    }

    fun isBiometricEnabled(): Boolean = preferencesRepository.isBiometricProtectionEnabled.blockingSingle()
}