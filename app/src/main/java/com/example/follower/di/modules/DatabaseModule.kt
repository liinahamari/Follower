package com.example.follower.di.modules

import android.content.Context
import androidx.room.Room
import com.example.follower.model.TrackDao
import com.example.follower.db.TracksDb
import com.example.follower.model.WayPointDao
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

private const val DATABASE_NAME_TRACKS = "database-tracks"

@Module
open class DatabaseModule {
    @Provides
    @Singleton
    open fun provideTracksDb(@Named(APP_CONTEXT) context: Context): TracksDb = Room.databaseBuilder(context, TracksDb::class.java, DATABASE_NAME_TRACKS)
        .build()

    @Provides
    @Singleton
    fun provideTrackDao(db: TracksDb): TrackDao = db.getTrackDao()

    @Provides
    @Singleton
    fun provideWayPointDao(db: TracksDb): WayPointDao = db.getWayPointDao()
}