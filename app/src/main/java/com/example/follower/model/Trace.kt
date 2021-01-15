package com.example.follower.model

data class Trace(val startTime: Long, val startTimeReadable: String, val points: List<TracePoint>)

data class TracePoint(val longitude: Double, val latitude: Double)