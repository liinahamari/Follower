package dev.liinahamari.follower.ext

import android.location.LocationManager

fun LocationManager.isGpsEnabled(): Boolean = allProviders.contains(LocationManager.GPS_PROVIDER) && isProviderEnabled(LocationManager.GPS_PROVIDER)
fun LocationManager.isNetworkLocationEnabled(): Boolean = allProviders.contains(LocationManager.NETWORK_PROVIDER) && isProviderEnabled(LocationManager.NETWORK_PROVIDER)