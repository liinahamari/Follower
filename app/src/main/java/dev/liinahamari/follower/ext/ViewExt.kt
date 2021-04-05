package dev.liinahamari.follower.ext

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.R

fun Context.getStatusBarHeight(): Int = resources.getIdentifier("status_bar_height", "dimen", "android")

fun Context.dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

fun Context.pxToDp(px: Int) = (px / resources.displayMetrics.density).toInt()

fun Context.isDarkModeEnabled(): Boolean = when (PreferenceManager.getDefaultSharedPreferences(this).getStringOf(getString(R.string.pref_theme))!!.toInt()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> deviceHasDarkThemeEnabled()
}

fun Fragment.isDarkModeEnabled(): Boolean = when (PreferenceManager.getDefaultSharedPreferences(requireContext()).getStringOf(getString(R.string.pref_theme))!!.toInt()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> deviceHasDarkThemeEnabled()
}

fun Context.adaptToNightModeState(drawable: Drawable?): Drawable? = drawable?.also {
    DrawableCompat.setTint(DrawableCompat.wrap(drawable), if (isDarkModeEnabled()) Color.WHITE else Color.BLACK)
}

fun Context.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
fun Fragment.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
fun Activity.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Drawable.changeColor(@ColorInt color: Int): Drawable = this
    .apply { DrawableCompat.setTint(DrawableCompat.wrap(this), color) }

@Suppress("DEPRECATION")
fun Activity.getScreenHeightPx(): Int = DisplayMetrics().apply { windowManager.defaultDisplay.getMetrics(this) }.heightPixels
@Suppress("DEPRECATION")
fun Fragment.getScreenHeightPx(): Int = DisplayMetrics().apply { requireActivity().windowManager.defaultDisplay.getMetrics(this) }.heightPixels