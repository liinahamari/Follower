package dev.liinahamari.follower.ext

import android.content.Context
import androidx.core.content.ContextCompat
import dev.liinahamari.follower.R
import dev.liinahamari.follower.screens.trace_map.Latitude
import dev.liinahamari.follower.screens.trace_map.Longitude
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
