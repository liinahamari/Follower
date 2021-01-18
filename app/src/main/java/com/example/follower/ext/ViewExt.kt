package com.example.follower.ext

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.follower.R
import es.dmoral.toasty.Toasty
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

fun Context.toast(message: String) = Toasty.error(this, message, Toast.LENGTH_LONG, true).show()
fun Fragment.toast(message: String) = context?.toast(message)
fun Activity.toast(message: String) = applicationContext.toast(message)

fun Context.getStatusBarHeight(): Int = resources.getIdentifier("status_bar_height", "dimen", "android")

fun MapView.standardMarker(long: Double, lat: Double) = Marker(this).apply {
    position = GeoPoint(lat, long)
    icon = ContextCompat.getDrawable(context, R.drawable.marker)
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
}