/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.follower.model

import androidx.annotation.VisibleForTesting
import androidx.room.*
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.TrackWithWayPoints
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface TrackDao {
    @Query("SELECT time FROM track")
    fun getAllIds(): Single<List<Long>>

    @Query("SELECT * FROM track")
    fun getAll(): Single<List<Track>>

    @VisibleForTesting
    @Query("SELECT COUNT(time) FROM track")
    fun getCount(): Int

    @Query("SELECT * FROM track WHERE time LIKE :taskId LIMIT 1")
    fun findByTrackId(taskId: Long): Single<Track>

    @Update
    fun update(track: Track): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(track: Track): Single<Long>

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
