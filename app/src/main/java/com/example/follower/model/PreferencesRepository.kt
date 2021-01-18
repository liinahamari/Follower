package com.example.follower.model

import android.content.Context
import android.content.SharedPreferences
import com.example.follower.R
import com.example.follower.ext.getStringOf
import com.example.follower.ext.writeStringOf
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class PreferencesRepository @Inject constructor(private val sharedPreferences: SharedPreferences, private val context: Context) {
    fun getPersistedLocale(): Single<PersistedLocaleResult> = Single.just(context.getString(R.string.pref_lang))
        .map { Locale(sharedPreferences.getStringOf(it) ?: Locale.UK.language) }
        .map<PersistedLocaleResult> { PersistedLocaleResult.Success(it) }
        .onErrorReturn { PersistedLocaleResult.Failure }

    fun getPersistedTrackRepresentation(): Single<PersistedTrackResult> = Single.just(context.getString(R.string.pref_track_representing))
        .map { sharedPreferences.getStringOf(it) }
        .map<PersistedTrackResult> {
            when (it) {
                context.getString(R.string.pref_marker_set) -> PersistedTrackResult.Success(context.getString(R.string.pref_marker_set))
                context.getString(R.string.pref_line) -> PersistedTrackResult.Success(context.getString(R.string.pref_line))
                else -> PersistedTrackResult.Success(context.getString(R.string.pref_line)).also {
                    sharedPreferences.writeStringOf(context.getString(R.string.pref_line), context.getString(R.string.pref_line))
                }
            }
        }
        .onErrorReturn { PersistedTrackResult.Failure }
}

sealed class PersistedLocaleResult {
    data class Success(val locale: Locale) : PersistedLocaleResult()
    object Failure : PersistedLocaleResult()
}

sealed class PersistedTrackResult {
    data class Success(val value: String) : PersistedTrackResult()
    object Failure : PersistedTrackResult()
}