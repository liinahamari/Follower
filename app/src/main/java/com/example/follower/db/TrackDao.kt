package com.example.follower.db

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.example.follower.screens.map.WayPoint
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAll(): Single<List<Track>>

    @VisibleForTesting
    @Query("SELECT COUNT(time) FROM track")
    fun getCount(): Int

    @Query("SELECT * FROM track WHERE time LIKE :taskId LIMIT 1")
    fun findByTrackId(taskId: Long): Single<Track>

    @Insert(onConflict = REPLACE)
    fun insert(counter: Track): Single<Long>

    @Insert
    fun insertAll(counters: List<Track>): Completable

    @Query("DELETE FROM track WHERE time = :id")
    fun delete(id: Long): Completable

    @Transaction
    @Query("SELECT * FROM track")
    fun getTrackWithWayPoints(): Single<List<TrackWithWayPoints>>
}

@Dao
interface WayPointDao {
    @Insert
    fun insertAll(counters: List<WayPoint>): Completable
}