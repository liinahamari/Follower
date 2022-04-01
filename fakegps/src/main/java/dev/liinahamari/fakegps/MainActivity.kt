package dev.liinahamari.fakegps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.fakegps.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit

private const val FAKE_GEOLOCATION_INTERVAL = 3L
private const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION

@RequiresApi(Build.VERSION_CODES.Q)
const val PERMISSION_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION

private const val LATITUDE_TALLINN = 59.436962
private const val LONGITUDE_TALLINN = 24.753574

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val ui by viewBinding(ActivityMainBinding::bind)

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { accepted -> accepted }) {
            startMockingLocation()
        } else {
            throw RuntimeException()
        }
    }

    private var actualLongitude = LONGITUDE_TALLINN
    private var actualLatitude = LATITUDE_TALLINN

    private val clicks = CompositeDisposable()
    private val mockDisposable = CompositeDisposable()
    private val locationManager: LocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupClicks()
    }

    override fun onDestroy() {
        super.onDestroy()
        clicks.clear()
        mockDisposable.clear()
    }

    private fun setupClicks() {
        clicks += ui.btnAddGPS.clicks()
            .throttleFirst()
            .subscribe {
                addTestProvider()
                ui.btnAddGPS.isEnabled = false
            }
        clicks += ui.btnDelGPS.clicks()
            .throttleFirst()
            .subscribe {
                removeTestProvider()
                ui.btnAddGPS.isEnabled = true
            }
    }

    private fun addTestProvider() {
        locationManager.apply {
            addTestProvider(
                LocationManager.GPS_PROVIDER,
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                Criteria.POWER_LOW,
                Criteria.ACCURACY_FINE
            )
            setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
        }

        val permissions = mutableListOf(PERMISSION_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(PERMISSION_BACKGROUND_LOCATION)
        }
        if (hasAllPermissions(permissions)) {
            startMockingLocation()
        } else {
            locationPermission.launch(permissions.toTypedArray())
        }
    }

    private fun removeTestProvider() = locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        .also {
            mockDisposable.clear()
            actualLatitude = LATITUDE_TALLINN
            actualLongitude = LONGITUDE_TALLINN
        }

    @SuppressLint("MissingPermission")
    private fun startMockingLocation() {
        mockDisposable += Observable.interval(FAKE_GEOLOCATION_INTERVAL, TimeUnit.SECONDS)
            .map {
                Location(LocationManager.GPS_PROVIDER)
                    .apply {
                        actualLatitude -= 0.0005
                        actualLongitude -= 0.0005
                        latitude = actualLatitude
                        longitude = actualLongitude
                        time = System.currentTimeMillis()
                        accuracy = 25f
                        elapsedRealtimeNanos = System.nanoTime()
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(locationManager::setGpsTestProviderLocation)
    }
}

private fun LocationManager.setGpsTestProviderLocation(location: Location) = setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
fun Observable<Unit>.throttleFirst(skipDurationMillis: Long = 500L): Observable<Unit> = compose { it.throttleFirst(skipDurationMillis, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }
fun Activity.hasAllPermissions(permissions: List<String>): Boolean = permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
