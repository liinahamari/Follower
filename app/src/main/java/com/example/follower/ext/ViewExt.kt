package com.example.follower.ext

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.follower.R
import com.example.follower.screens.trace_map.Latitude
import com.example.follower.screens.trace_map.Longitude
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

fun Context.getStatusBarHeight(): Int = resources.getIdentifier("status_bar_height", "dimen", "android")

fun MapView.createFollowerMarker(long: Longitude, lat: Latitude) = Marker(this).apply {
    position = GeoPoint(lat, long)
    icon = ContextCompat.getDrawable(context, R.drawable.marker)
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
}

fun MapView.createStandardMarker(long: Double, lat: Double) = Marker(this).apply {
    position = GeoPoint(lat, long)
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
}

fun Context.dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

fun Context.pxToDp(px: Int) = (px / resources.displayMetrics.density).toInt()

fun Context.adaptToNightModeState(drawable: Drawable?): Drawable? = drawable?.also {
    val isNightMode: Boolean = when (PreferenceManager.getDefaultSharedPreferences(this).getStringOf(getString(R.string.pref_theme))!!.toInt()) {
        AppCompatDelegate.MODE_NIGHT_NO -> false
        AppCompatDelegate.MODE_NIGHT_YES -> true
        else -> deviceHasDarkThemeEnabled()
    }
    DrawableCompat.setTint(DrawableCompat.wrap(drawable), if (isNightMode) Color.WHITE else Color.BLACK)
}

fun Context.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
fun Fragment.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
fun Activity.deviceHasDarkThemeEnabled() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
