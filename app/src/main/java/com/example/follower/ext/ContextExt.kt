package com.example.follower.ext

import android.app.ActivityManager
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
): Context {
    val locales = resources.getStringArray(R.array.supported_locales)
    val newLocale = Locale(locales.firstOrNull { it == persistedLanguage } ?: locales.firstOrNull { it == defaultLocale } ?: Locale.UK.language)
    saveAppLocale(newLocale.language)
    Locale.setDefault(newLocale)
    return createConfigurationContext(Configuration().apply { setLocale(newLocale) })
}

private fun Context.getDefaultSharedPreferences(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
private fun Context.getSavedAppLocale(): String = getDefaultSharedPreferences().getStringOf(getString(R.string.pref_lang))!!
private fun Context.saveAppLocale(newLocale: String) = getDefaultSharedPreferences().writeStringOf(getString(R.string.pref_lang), newLocale)

@Suppress("DEPRECATION"
    /** """this method is no longer available to third party applications""" -- but we don't care tracking our application's services*/)
fun Context.isServiceRunning(serviceClass: Class<*>) = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getRunningServices(Int.MAX_VALUE).any { serviceClass.name == it.service.className }
