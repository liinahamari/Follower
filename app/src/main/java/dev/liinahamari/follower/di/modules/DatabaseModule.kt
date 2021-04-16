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

package dev.liinahamari.follower.di.modules

import android.content.Context
import androidx.room.Room
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.db.TracksDb
import dev.liinahamari.follower.model.WayPointDao
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