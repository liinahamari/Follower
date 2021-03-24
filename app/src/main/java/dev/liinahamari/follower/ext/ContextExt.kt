package dev.liinahamari.follower.ext

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.R
import dev.liinahamari.follower.screens.logs.FILE_PROVIDER_META
import java.io.File
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

fun Context.getDefaultSharedPreferences(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
private fun Context.getSavedAppLocale(): String = getDefaultSharedPreferences().getStringOf(getString(R.string.pref_lang))!!
private fun Context.saveAppLocale(newLocale: String) = getDefaultSharedPreferences().writeStringOf(getString(R.string.pref_lang), newLocale)

@Suppress(
    "DEPRECATION"
    /** """this method is no longer available to third party applications""" -- but we do care only about tracking our application's services*/
)
fun Context.isServiceRunning(serviceClass: Class<*>) = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getRunningServices(Int.MAX_VALUE).any { serviceClass.name == it.service.className }

fun FragmentActivity.openAppSettings() = startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${BuildConfig.APPLICATION_ID}")))

fun Fragment.startService(serviceClass: Class<out Service>, bundle: Bundle? = null, action: String? = null) = requireActivity().applicationContext.startService(Intent(requireActivity().applicationContext, serviceClass).apply {
    bundle?.let { putExtras(it) }
    action?.let { this.action = action }
})

fun Fragment.stopService(serviceClass: Class<out Service>) = requireActivity().application.stopService(Intent(requireActivity().applicationContext, serviceClass))

private fun Context.createDirIfNotExist(dirName: String) = File(filesDir, dirName).apply {
    if (exists().not()) {
        mkdir()
    }
}

fun Context.createFileIfNotExist(fileName: String, dirName: String) = File(createDirIfNotExist(dirName), fileName).apply {
    if (exists().not()) {
        createNewFile()
    }
}

fun Context.getUriForInternalFile(file: File): Uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + FILE_PROVIDER_META, file)

fun Context.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
fun Fragment.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
fun Activity.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Preference.setColorDependantOnNightMode(isNightMode: Boolean) = DrawableCompat.setTint(DrawableCompat.wrap(icon), if (isNightMode) Color.WHITE else Color.BLACK)