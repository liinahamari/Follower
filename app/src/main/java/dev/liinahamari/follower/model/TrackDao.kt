package dev.liinahamari.follower.model

import androidx.paging.DataSource
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.TrackWithWayPoints
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TrackDao {
    @Query("SELECT * FROM track")
    fun getAll(): DataSource.Factory<Int, Track>

    @Query("SELECT COUNT(time) FROM track")
    fun getCount(): Int

    @Query("SELECT * FROM track WHERE time LIKE :taskId LIMIT 1")
    fun findByTrackId(taskId: Long): Single<Track>

    @Update
    fun update(track: Track): Completable

    @Insert(onConflict = REPLACE)
    fun insert(counter: Track): Single<Long>

    @Insert
    fun insertAll(counters: List<Track>): Completable

    @Query("DELETE FROM track WHERE time = :id")
    fun delete(id: Long): Completable

    @Transaction
    @Query("SELECT * FROM track")
    fun getAllTracksWithWayPoints(): DataSource.Factory<Int, TrackWithWayPoints>

    @Transaction
    @Query("SELECT * FROM track WHERE time LIKE :trackId LIMIT 1")
    fun getTrackWithWayPoints(trackId: Long): Single<TrackWithWayPoints>
}