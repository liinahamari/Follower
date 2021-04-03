package dev.liinahamari.follower.ext

import androidx.core.content.ContextCompat
import dev.liinahamari.follower.R
import dev.liinahamari.follower.screens.trace_map.Latitude
import dev.liinahamari.follower.screens.trace_map.Longitude
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

enum class MarkerType {
    START, END, STANDARD, WAYPOINT
}

fun MapView.createMarker(long: Longitude, lat: Latitude, markerType: MarkerType, time: String) = Marker(this).apply {
    position = GeoPoint(lat, long)
    when (markerType) {
        MarkerType.START -> icon = ContextCompat.getDrawable(context, R.drawable.ic_track_start)
        MarkerType.END -> icon = ContextCompat.getDrawable(context, R.drawable.ic_track_end)
        MarkerType.STANDARD -> Unit
        MarkerType.WAYPOINT -> icon = ContextCompat.getDrawable(context, R.drawable.marker)
    }
    title = time
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
}