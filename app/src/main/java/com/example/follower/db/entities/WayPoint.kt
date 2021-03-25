package com.example.follower.db.entities

import android.location.Location
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.example.follower.ext.round

@Entity(foreignKeys = [ForeignKey(onDelete = CASCADE, entity = Track::class, parentColumns = ["time"], childColumns = ["trackId"])])
data class WayPoint(
    val trackId: Long,
    val provider: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 1f,
    @PrimaryKey val time: Long
)

fun Location.toWayPoint(trackId: Long): WayPoint = WayPoint(trackId = trackId, provider = provider, longitude = longitude.round(6), latitude = latitude.round(6), time = System.currentTimeMillis())
