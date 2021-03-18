package com.example.follower

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.example.follower.db.TracksDb
import com.example.follower.di.components.DaggerAppComponent
import com.example.follower.di.modules.DatabaseModule
import com.example.follower.di.modules.ServiceModule
import com.example.follower.services.location_tracking.ACTION_START_TRACKING
import com.example.follower.services.location_tracking.LocationTrackingService
import dagger.Module
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

private const val SOME_LAT = .1
private const val SOME_LON = .2
private const val SOME_PROV = "prov"

@RunWith(AndroidJUnit4::class)
class SampleServiceTest {
    @get:Rule
    var serviceRule = ServiceTestRule()

    @Test
    fun testService() = serviceRule.startService(Intent(getApplicationContext(), LocationTrackingService::class.java))

    @Test
    fun testBoundService() {
        val binder: IBinder = serviceRule.bindService(Intent(getApplicationContext(), LocationTrackingService::class.java))
        val service: LocationTrackingService = (binder as LocationTrackingService.LocationServiceBinder).getService()
        assertNotNull(service)
    }

    @Test
    fun startTracking__ACTION_START_TRACKING__setup() {
        val db = Room.inMemoryDatabaseBuilder(getApplicationContext<FollowerApp>(), TracksDb::class.java).allowMainThreadQueries().build()

        val app = getApplicationContext<FollowerApp>().apply {
            setupDagger(DaggerAppComponent
                .builder()
                .application(this)
                .serviceModule(FakeServiceModule(createMockedLocationManager()))
                .dbModule(FakeDbModule(db))
                .build())
        }

        serviceRule.startService(
            Intent(app, LocationTrackingService::class.java)
                .apply {
                    action = ACTION_START_TRACKING
                })

        val binder: IBinder = serviceRule.bindService(Intent(app, LocationTrackingService::class.java))
        val service: LocationTrackingService = (binder as LocationTrackingService.LocationServiceBinder).getService()
        assertNotNull(service)
        assert(service.isTracking.value == true)
        assert(service.isTrackEmpty)
        assert(db.getTrackDao().getCount() == 1)
        assert(db.getWayPointDao().getCount() == 0)
    }
    
    private fun createMockedLocationManager(): LocationManager {
        val loc = mock(Location::class.java).apply {
            `when`(longitude).thenReturn(SOME_LON)
            `when`(latitude).thenReturn(SOME_LAT)
            `when`(provider).thenReturn(SOME_PROV)
        }
        return mock(LocationManager::class.java).apply {
            `when`(getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(loc)
            `when`(getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(loc)
        }
    }
}

@Module
class FakeServiceModule(private val lockMng: LocationManager) : ServiceModule() {
    override fun provideLocationManager(context: Context): LocationManager = lockMng
}

@Module
class FakeDbModule(private val db: TracksDb): DatabaseModule(){
    override fun provideMissedAlarmsCountersDatabase(context: Context): TracksDb = db
}