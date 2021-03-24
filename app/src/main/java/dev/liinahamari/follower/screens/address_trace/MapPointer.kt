package dev.liinahamari.follower.screens.address_trace

import dev.liinahamari.follower.screens.trace_map.Latitude
import dev.liinahamari.follower.screens.trace_map.Longitude

data class MapPointer(val commonAddress: String, val lat: Latitude, val lon: Longitude)