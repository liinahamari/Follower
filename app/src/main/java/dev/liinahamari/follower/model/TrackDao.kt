package dev.liinahamari.follower.model

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.TrackWithWayPoints
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
    fun getAllTracksWithWayPoints(): Single<List<TrackWithWayPoints>>

    @Transaction
    @Query("SELECT * FROM track WHERE time LIKE :trackId LIMIT 1")
    fun getTrackWithWayPoints(trackId: Long): Single<TrackWithWayPoints>
}