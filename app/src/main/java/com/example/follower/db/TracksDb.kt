package com.example.follower.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.follower.model.TrackDao
import com.example.follower.model.WayPointDao
import com.example.follower.db.entities.Track
import com.example.follower.db.entities.WayPoint

@Database(entities = [Track::class, WayPoint::class], version = 1)
abstract class TracksDb : RoomDatabase() {
    abstract fun getTrackDao(): TrackDao
    abstract fun getWayPointDao(): WayPointDao
}
