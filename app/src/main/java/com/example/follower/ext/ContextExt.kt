package com.example.follower.ext

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.example.follower.R
import java.util.*

fun Configuration.getLocalesLanguage(): String = locales[0].language

fun Context.provideUpdatedContextWithNewLocale(
    persistedLanguage: String? = kotlin.runCatching { getSavedAppLocale() }.getOrNull(),
    defaultLocale: String? = null
): Context { /*TODO RTL*/
    val locales = resources.getStringArray(R.array.supported_locales)
    val newLocale = Locale(locales.firstOrNull { it == persistedLanguage } ?: locales.firstOrNull { it == defaultLocale } ?: Locale.UK.language)
    saveAppLocale(newLocale.language)
    Locale.setDefault(newLocale)
    return createConfigurationContext(Configuration().apply { setLocale(newLocale) })
}

private fun Context.getDefaultSharedPreferences(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
private fun Context.getSavedAppLocale(): String = getDefaultSharedPreferences().getStringOf(getString(R.string.pref_lang))!!
private fun Context.saveAppLocale(newLocale: String) = getDefaultSharedPreferences().writeStringOf(getString(R.string.pref_lang), newLocale)
