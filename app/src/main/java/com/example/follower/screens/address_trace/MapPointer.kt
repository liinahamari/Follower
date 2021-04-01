package com.example.follower.screens.address_trace

import com.example.follower.screens.trace_map.Latitude
import com.example.follower.screens.trace_map.Longitude

data class MapPointer(val commonAddress: String, val lat: Latitude, val lon: Longitude, val time: String)