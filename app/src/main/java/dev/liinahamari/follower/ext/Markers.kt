/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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