package dev.liinahamari.follower.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.liinahamari.follower.db.entities.WayPoint
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface WayPointDao {
    @Query("SELECT * FROM waypoint WHERE trackId = :trackId")
    fun getAllByTrackId(trackId: Long): LiveData<List<WayPoint>>

    @Query("SELECT provider FROM waypoint WHERE trackId = :trackId")
    fun validateWpAmount(trackId: Long): Single<List<String>>

    @Insert
    fun insert(waypoint: WayPoint): Completable

    @Insert
    fun insertAll(waypoints: List<WayPoint>): Completable

    @Query("DELETE FROM waypoint WHERE trackId = :trackId")
    fun delete(trackId: Long): Completable

    @Query("SELECT COUNT(time) FROM waypoint")
    fun getCount(): Int
}