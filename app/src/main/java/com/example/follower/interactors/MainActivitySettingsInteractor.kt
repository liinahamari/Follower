package com.example.follower.interactors

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.follower.R
import com.example.follower.ext.getStringOf
import com.example.follower.ext.writeStringOf
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

class MainActivitySettingsInteractor @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val baseComposers: BaseComposers,
    private val logger: FlightRecorder,
    private val context: Context
) {
    fun handleThemeChanges(toBeCompared: Int): Maybe<NightModeChangesResult> = Single.fromCallable {
            kotlin.runCatching { sharedPreferences.getStringOf(context.getString(R.string.pref_theme)) }.getOrThrow()
        }
        .map { it.toInt() }
        .filter { it != toBeCompared }
        .map<NightModeChangesResult> { if (it == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM || it == AppCompatDelegate.MODE_NIGHT_NO || it == AppCompatDelegate.MODE_NIGHT_YES) NightModeChangesResult.Success(it) else NightModeChangesResult.Success(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
        .onErrorReturn {
            if (it is NullPointerException) { /**pref_theme contains null: doing initial setup... */
                try {
                    with (AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                        sharedPreferences.writeStringOf(context.getString(R.string.pref_theme), this.toString())
                        NightModeChangesResult.Success(this)
                    }
                } catch (e: Throwable) {
                    NightModeChangesResult.SharedChangesCorruptionError
                }
            } else {
                NightModeChangesResult.SharedChangesCorruptionError
            }
        }.onErrorReturn { NightModeChangesResult.SharedChangesCorruptionError }
        .doOnError { logger.e(label = "Problem with changing theme!", stackTrace = it.stackTrace) }
        .compose(baseComposers.applyMaybeSchedulers())

    fun checkLocaleChanged(currentLocale: String): Maybe<LocaleChangedResult> = Single.just(currentLocale)
        .filter { sharedPreferences.getStringOf(context.getString(R.string.pref_lang)).equals(it).not() }
        .map<LocaleChangedResult> { LocaleChangedResult.Success }
        .onErrorReturn { LocaleChangedResult.SharedPreferencesCorruptionError }
        .doOnError { logger.e(label = "locale change", stackTrace = it.stackTrace) }
        .compose(baseComposers.applyMaybeSchedulers())
}

sealed class NightModeChangesResult {
    data class Success(val code: Int) : NightModeChangesResult()
    object SharedChangesCorruptionError : NightModeChangesResult()
}

sealed class LocaleChangedResult {
    object Success : LocaleChangedResult()
    object SharedPreferencesCorruptionError : LocaleChangedResult()
}
