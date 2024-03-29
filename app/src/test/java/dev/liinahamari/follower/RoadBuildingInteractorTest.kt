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
*//*


package dev.liinahamari.follower

import android.os.Build
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dev.liinahamari.follower.db.TracksDb
import dev.liinahamari.follower.db.entities.Track
import dev.liinahamari.follower.db.entities.WayPoint
import dev.liinahamari.follower.ext.getDefaultSharedPreferences
import dev.liinahamari.follower.ext.writeStringOf
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.helper.rx.TestSchedulers
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.model.WayPointDao
import dev.liinahamari.follower.screens.trace_map.GetRoadResult
import dev.liinahamari.follower.screens.trace_map.RoadBuildingInteractor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MOCKED_TRACK_ID = 1L

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class RoadBuildingInteractorTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val sharedPrefs = context.getDefaultSharedPreferences()
    private val prefTrackRepresentationId = context.getString(R.string.pref_track_representation)
    private lateinit var db: TracksDb
    private lateinit var trackDao: TrackDao
    private lateinit var wayPointDao: WayPointDao
    private lateinit var roadBuildingInteractor: RoadBuildingInteractor

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(context, TracksDb::class.java).allowMainThreadQueries().build()
        trackDao = db.getTrackDao()
        wayPointDao = db.getWayPointDao()
        roadBuildingInteractor = RoadBuildingInteractor(context, trackDao, baseComposers, PreferencesRepository(sharedPrefs, context, baseComposers), OSRMRoadManager(context))

        trackDao.insert(Track(MOCKED_TRACK_ID, "title")).subscribe()
        wayPointDao.insertAll(
            listOf(
                WayPoint(MOCKED_TRACK_ID, "prov", .1, .2, time = System.currentTimeMillis())
            )
        ).subscribe()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `if Track Representation setting is pref_line, then return SuccessLine with Road object inside`() {
        sharedPrefs.writeStringOf(prefTrackRepresentationId, context.getString(R.string.pref_line))
        assert(trackDao.getCount() == 1)
        assert(wayPointDao.getCount() == 1)

        roadBuildingInteractor.getRoad(MOCKED_TRACK_ID)
            .test()
            .assertComplete()
            .assertNoErrors()
//            .assertValue { it is GetRoadResult.SuccessfulLine && it.road.mRouteHigh.size == 1 }
    }

    @Test
    fun `if Track Representation setting is pref_set, then return SuccessMarkerSet`() {
        sharedPrefs.writeStringOf(prefTrackRepresentationId, context.getString(R.string.pref_marker_set))
        assert(trackDao.getCount() == 1)
        assert(wayPointDao.getCount() == 1)

        roadBuildingInteractor.getRoad(MOCKED_TRACK_ID)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it is GetRoadResult.SuccessfulMarkerSet && it.markerSet.wayPoints.size == 1 }
    }
}*/
