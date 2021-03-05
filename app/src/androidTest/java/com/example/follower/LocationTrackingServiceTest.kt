package com.example.follower

import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.example.follower.services.LocationTrackingService
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
}