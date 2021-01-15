package com.example.follower.model

import android.content.Context
import android.content.SharedPreferences
import com.example.follower.R
import com.example.follower.ext.getStringOf
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

class PreferencesRepository @Inject constructor(private val sharedPreferences: SharedPreferences, private val context: Context) {
    fun getPersistedLocale(): Single<PersistedLocaleResult> = Single.just(context.getString(R.string.pref_lang))
        .map { Locale(sharedPreferences.getStringOf(it) ?: Locale.UK.language) }
        .map<PersistedLocaleResult> { PersistedLocaleResult.Success(it) }
        .onErrorReturn { PersistedLocaleResult.Failure }
}

sealed class PersistedLocaleResult {
    data class Success(val locale: Locale) : PersistedLocaleResult()
    object Failure : PersistedLocaleResult()
}