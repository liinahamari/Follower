package com.example.follower.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.follower.db.Track
import com.example.follower.db.TrackDao
import com.example.follower.screens.map.WayPoint

@Database(entities = [Track::class, WayPoint::class], version = 1)
abstract class TracksDb : RoomDatabase() {
    abstract fun getTrackDao(): TrackDao
    abstract fun getWayPointDao(): WayPointDao
}
