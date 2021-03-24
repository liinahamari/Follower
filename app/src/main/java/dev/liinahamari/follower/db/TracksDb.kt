package dev.liinahamari.follower.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.model.WayPointDao
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.WayPoint

@Database(entities = [Track::class, WayPoint::class], version = 1)
abstract class TracksDb : RoomDatabase() {
    abstract fun getTrackDao(): TrackDao
    abstract fun getWayPointDao(): WayPointDao
}
