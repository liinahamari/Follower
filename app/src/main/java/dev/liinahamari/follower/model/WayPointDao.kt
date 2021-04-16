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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.liinahamari.follower.db.entities.WayPoint
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

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