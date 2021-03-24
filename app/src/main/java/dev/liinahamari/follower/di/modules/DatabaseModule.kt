package dev.liinahamari.follower.di.modules

import android.content.Context
import androidx.room.Room
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.db.TracksDb
import dev.liinahamari.follower.model.WayPointDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

private const val DATABASE_NAME_TRACKS = "database-tracks"

@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideMissedAlarmsCountersDatabase(context: Context): TracksDb = Room.databaseBuilder(context, TracksDb::class.java, DATABASE_NAME_TRACKS)
        .build()

    @Provides
    @Singleton
    fun provideTrackDao(db: TracksDb): TrackDao = db.getTrackDao()

    @Provides
    @Singleton
    fun provideWayPointDao(db: TracksDb): WayPointDao = db.getWayPointDao()
}