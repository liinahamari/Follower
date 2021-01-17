package com.example.follower.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.follower.screens.map.WayPoint

@Entity
data class Track(@PrimaryKey val time: Long, val title: String)

data class TrackWithWayPoints(
    @Embedded val user: Track,
    @Relation(parentColumn = "time", entityColumn = "trackId") val wayPoints: List<WayPoint>
)