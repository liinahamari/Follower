package com.example.follower.model

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.follower.db.entities.WayPoint
import io.reactivex.Completable

@Dao
interface WayPointDao {
    @Insert
    fun insertAll(waypoints: List<WayPoint>): Completable

    @Query("DELETE FROM waypoint WHERE trackId = :trackId")
    fun delete(trackId: Long): Completable

    @VisibleForTesting
    @Query("SELECT COUNT(time) FROM waypoint")
    fun getCount(): Int
}