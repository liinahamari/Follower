package dev.liinahamari.follower.model

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.ext.*
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Named

private const val DEFAULT_AUTO_TRACKING_START_TIME = "09:00"
private const val DEFAULT_AUTO_TRACKING_END_TIME = "21:00"

class PreferencesRepository @Inject constructor(private val sharedPreferences: SharedPreferences, @Named(APP_CONTEXT) private val context: Context, private val baseComposers: BaseComposers) {
    /** If App is launching first time, then set default preferences*/
    fun applyDefaultPreferences(): Completable = Completable.fromCallable {
            if (sharedPreferences.getBoolean(context.getString(R.string.pref_is_first_launch), false).not()) {
                sharedPreferences.writeBooleanOf(context.getString(R.string.pref_is_first_launch), true)
                sharedPreferences.writeBooleanOf(context.getString(R.string.pref_battery_optimization), context.isIgnoringBatteryOptimizations())

                sharedPreferences.writeBooleanOf(context.getString(R.string.pref_acra_disable), true)
                sharedPreferences.writeStringOf(context.getString(R.string.pref_uid), UUID.randomUUID().toString())

                PreferenceManager.setDefaultValues(context, R.xml.preferences, false)
                sharedPreferences.writeIntOf(context.getString(R.string.pref_tracking_start_time), hourlyTimeToMinutesFromMidnight(DEFAULT_AUTO_TRACKING_START_TIME))
                sharedPreferences.writeIntOf(context.getString(R.string.pref_tracking_stop_time), hourlyTimeToMinutesFromMidnight(DEFAULT_AUTO_TRACKING_END_TIME))
            }
        }

    fun getTrackDisplayMode(): Single<TrackDisplayModeResult> = Single.just(context.getString(R.string.pref_track_display_mode))
        .map { sharedPreferences.getStringOf(it) ?: context.getString(R.string.pref_value_track_display_mode_none) }
        .map<TrackDisplayModeResult> { TrackDisplayModeResult.Success(it) }
        .onErrorReturn { TrackDisplayModeResult.Failure }
        .compose(baseComposers.applySingleSchedulers())

    fun saveTrackDisplayMode(mode: String): Completable = Completable.fromCallable { sharedPreferences.writeStringOf(context.getString(R.string.pref_track_display_mode), mode) }
        .compose(baseComposers.applyCompletableSchedulers())

    fun getPersistedLocale(): Single<PersistedLocaleResult> = Single.just(context.getString(R.string.pref_lang))
        .map { Locale(sharedPreferences.getStringOf(it) ?: Locale.UK.language) }
        .map<PersistedLocaleResult> { PersistedLocaleResult.Success(it) }
        .onErrorReturn { PersistedLocaleResult.Failure }

    fun getPersistedTrackRepresentation(): Single<PersistedTrackResult> = Single.just(context.getString(R.string.pref_track_representation))
        .map { sharedPreferences.getStringOf(it) }
        .onErrorResumeNext { Single.fromCallable { sharedPreferences.writeStringOf(context.getString(R.string.pref_track_representation), context.getString(R.string.pref_line)) }
            .map { sharedPreferences.getStringOf(context.getString(R.string.pref_track_representation)) } }
        .map<PersistedTrackResult> {
            when (it) {
                context.getString(R.string.pref_marker_set) -> PersistedTrackResult.Success(context.getString(R.string.pref_marker_set))
                context.getString(R.string.pref_line) -> PersistedTrackResult.Success(context.getString(R.string.pref_line))
                else -> PersistedTrackResult.Success(context.getString(R.string.pref_line)).also {
                    sharedPreferences.writeStringOf(context.getString(R.string.pref_track_representation), context.getString(R.string.pref_line))
                }
            }
        }
        .onErrorReturn { PersistedTrackResult.Failure }

    fun incrementAppLaunchCounter() = sharedPreferences.incrementAppLaunchCounter(context)
}

sealed class PersistedLocaleResult {
    data class Success(val locale: Locale) : PersistedLocaleResult()
    object Failure : PersistedLocaleResult()
}

sealed class TrackDisplayModeResult {
    data class Success(val displayMode: String) : TrackDisplayModeResult()
    object Failure : TrackDisplayModeResult()
}

sealed class PersistedTrackResult {
    data class Success(val value: String) : PersistedTrackResult()
    object Failure : PersistedTrackResult()
}