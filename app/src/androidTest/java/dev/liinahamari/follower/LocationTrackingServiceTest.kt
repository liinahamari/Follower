package dev.liinahamari.follower

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.example.follower.di.components.DaggerAppComponent
import dev.liinahamari.follower.di.modules.ServiceModule
import dev.liinahamari.follower.services.ACTION_START_TRACKING
import dev.liinahamari.follower.services.LocationTrackingService
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
    fun ifSomeLocationValueCached_Network_or_GPS_thenAddItAsFirstWayPoint() {
        val app = getApplicationContext<FollowerApp>().apply {
            setupDagger(DaggerAppComponent
                .builder()
                .application(this)
                .serviceModule(FakeServiceModule(createMockedLocationManager()))
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

        assert(service.wayPoints.isNotEmpty())
        assert(service.wayPoints.size == 1)
        assert(service.wayPoints.first().longitude == SOME_LON)
        assert(service.wayPoints.first().latitude == SOME_LAT)
        assert(service.wayPoints.first().provider == SOME_PROV)
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