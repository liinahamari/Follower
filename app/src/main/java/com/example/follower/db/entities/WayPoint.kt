package com.example.follower.db.entities

import android.location.Location
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(onDelete = CASCADE, entity = Track::class, parentColumns = ["time"], childColumns = ["trackId"])])
data class WayPoint(
    @Volatile var trackId: Long,
    val provider: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 1f,
    @PrimaryKey val time: Long
)

fun Location.toWayPoint(trackId: Long): WayPoint = WayPoint(trackId = trackId, provider = provider, longitude = longitude, latitude = latitude, time = System.currentTimeMillis())