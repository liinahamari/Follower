package com.example.follower

import android.os.Build
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.follower.db.TracksDb
import com.example.follower.db.entities.Track
import com.example.follower.db.entities.WayPoint
import com.example.follower.ext.getDefaultSharedPreferences
import com.example.follower.ext.writeStringOf
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.helper.rx.TestSchedulers
import com.example.follower.model.PreferencesRepository
import com.example.follower.model.TrackDao
import com.example.follower.model.WayPointDao
import com.example.follower.screens.trace_map.GetRoadResult
import com.example.follower.screens.trace_map.RoadBuildingInteractor
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
    private val baseComposers = BaseComposers(TestSchedulers(), FlightRecorder(createTempFile()))
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
            .assertValue { it is GetRoadResult.SuccessfulLine && it.road.mRouteHigh.size == 1 }
    }

    @Test
    fun `if Track Representation setting is pref_set, then return SuccessMarkerSet with markerSet field containing the same amount wayPoints as tied to exact Track`() {
        sharedPrefs.writeStringOf(prefTrackRepresentationId, context.getString(R.string.pref_marker_set))
        assert(trackDao.getCount() == 1)
        assert(wayPointDao.getCount() == 1)

        roadBuildingInteractor.getRoad(MOCKED_TRACK_ID)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it is GetRoadResult.SuccessfulMarkerSet && it.markerSet.size == 1 }
    }
}