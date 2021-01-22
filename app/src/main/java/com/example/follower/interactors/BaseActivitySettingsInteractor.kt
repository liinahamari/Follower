package com.example.follower.interactors

import android.content.Context
import android.content.SharedPreferences
import com.example.follower.R
import com.example.follower.ext.getStringOf
import com.example.follower.ext.writeStringOf
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

class BaseActivitySettingsInteractor @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val baseComposers: BaseComposers,
    private val logger: FlightRecorder,
    private val context: Context
) {
    fun handleThemeChanges(toBeCompared: Int): Maybe<NightModeChangesResult> = Single.fromCallable {
            kotlin.runCatching { sharedPreferences.getStringOf(context.getString(R.string.pref_theme)) }.getOrNull()
        }
        .filter { it != toBeCompared.toString() }
        .map { it.toInt() }
        .map<NightModeChangesResult> { NightModeChangesResult.Success(it) }
        .onErrorReturn {
            if (it is NullPointerException) {
                try {
                    sharedPreferences.writeStringOf(context.getString(R.string.pref_theme), toBeCompared.toString())
                    NightModeChangesResult.Success(toBeCompared)
                } catch (e: Throwable) {
                    NightModeChangesResult.SharedChangesCorruptionError
                }
            } else {
                NightModeChangesResult.SharedChangesCorruptionError
            }
        }.onErrorReturn { NightModeChangesResult.SharedChangesCorruptionError }
        .doOnError {
            logger.wtf { "Problem with changing theme!" }
            logger.e(stackTrace = it.stackTrace)
        }
        .compose(baseComposers.applyMaybeSchedulers())

    fun checkLocaleChanged(currentLocale: String): Maybe<LocaleChangedResult> = Single.just(currentLocale)
        .filter { sharedPreferences.getStringOf(context.getString(R.string.pref_lang)).equals(it).not() }
        .map<LocaleChangedResult> { LocaleChangedResult.Success }
        .onErrorReturn { LocaleChangedResult.SharedPreferencesCorruptionError }
        .doOnError {
            logger.wtf { "Problem with locale changes handling!" }
            logger.e(stackTrace = it.stackTrace)
        }
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
