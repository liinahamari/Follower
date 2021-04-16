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

package dev.liinahamari.follower

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import dagger.Module
import dev.liinahamari.follower.db.TracksDb
import dev.liinahamari.follower.di.components.DaggerAppComponent
import dev.liinahamari.follower.di.modules.DatabaseModule
import dev.liinahamari.follower.di.modules.ServiceModule
import dev.liinahamari.follower.services.location_tracking.ACTION_START_TRACKING
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
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
    override fun provideTracksDb(context: Context): TracksDb = db
}