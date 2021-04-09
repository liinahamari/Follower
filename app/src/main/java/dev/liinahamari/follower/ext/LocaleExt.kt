package dev.liinahamari.follower.ext

import android.content.Context
import android.content.res.Configuration
import dev.liinahamari.follower.R
import java.util.*

fun Configuration.getLocalesLanguage(): String = locales[0].language

/** On first launch getSavedAppLocale() returns null, and then app seek corresponding to device provided locale to apply the corresponding translations.
 * If device has unsupported locale (such as German), application applies English locale */
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

private fun Context.getSavedAppLocale(): String = getDefaultSharedPreferences().getStringOf(getString(R.string.pref_lang))!!
private fun Context.saveAppLocale(newLocale: String) = getDefaultSharedPreferences().writeStringOf(getString(R.string.pref_lang), newLocale)

