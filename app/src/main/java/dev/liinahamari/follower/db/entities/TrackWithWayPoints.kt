package dev.liinahamari.follower.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TrackWithWayPoints(
    @Embedded val track: Track,
    @Relation(parentColumn = "time", entityColumn = "trackId") val wayPoints: List<WayPoint>
)