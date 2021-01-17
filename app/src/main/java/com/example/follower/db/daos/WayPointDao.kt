package com.example.follower.db.daos

import androidx.room.Dao
import androidx.room.Insert
import com.example.follower.db.entities.WayPoint
import io.reactivex.Completable

@Dao
interface WayPointDao {
    @Insert
    fun insertAll(counters: List<WayPoint>): Completable
}