package com.example.follower.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.follower.db.entities.WayPoint
import io.reactivex.Completable

@Dao
interface WayPointDao {
    @Insert
    fun insertAll(waypoints: List<WayPoint>): Completable

    /*TODO cascade delete https://stackoverflow.com/questions/46021529/cascade-delete-based-on-foreignkey-in-android-rooms-orm*/
    @Query("DELETE FROM waypoint WHERE trackId = :trackId")
    fun delete(trackId: Long): Completable
}