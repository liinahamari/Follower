package com.example.follower

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.follower.ext.getStringOf
import com.example.follower.ext.writeStringOf
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

fun Activity.handleUsersReactionToPermission(
    permissionToHandle: String,
    allPermissions: Array<out String>,
    doIfDenied: () -> Unit,
    doIfAllowed: () -> Unit,
    doIfNeverAskAgain: () -> Unit
) {
    if (allPermissions.contains(permissionToHandle)) {
        if (shouldShowRequestPermissionRationale(permissionToHandle)) {
            doIfDenied()
        } else {
            if (hasPermission(permissionToHandle)) {
                doIfAllowed()
            } else {
                doIfNeverAskAgain()
            }
        }
    }
}

fun Fragment.handleUsersReactionToPermission(permissionToHandle: String,
                                             allPermissions: Array<out String>,
                                             doIfDenied: () -> Unit,
                                             doIfAllowed: () -> Unit,
                                             doIfNeverAskAgain: () -> Unit) = requireActivity()
    .handleUsersReactionToPermission(permissionToHandle, allPermissions, doIfDenied, doIfAllowed, doIfNeverAskAgain)

fun Activity.hasPermission(permission: String) = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.hasAllPermissions(permissions: Array<String>): Boolean = permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

/** Only for RxView elements!*/
fun Observable<Unit>.throttleFirst(): Observable<Unit> = compose { it.throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }

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
