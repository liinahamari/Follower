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
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

private const val FAKE_GEOLOCATION_INTERVAL = 3L
private const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
@RequiresApi(Build.VERSION_CODES.Q) const val PERMISSION_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val geoPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.all { accepted -> accepted }) {
            startMockingLocation()
        } else {
            throw java.lang.RuntimeException()
        }
    }

    private var lat = 55.75578
    private var lon = 37.61786

    private val clicks = CompositeDisposable()
    private val mockDisposable = CompositeDisposable()
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setupClicks()
    }

    override fun onDestroy() {
        super.onDestroy()
        clicks.clear()
    }

    private fun setupClicks() {
        clicks += btnAddGPS.clicks()
            .throttleFirst()
            .subscribe({
                addGPS()
                btnAddGPS.isEnabled = false
            }, { it.printStackTrace() })
        clicks += btnDelGPS.clicks()
            .throttleFirst()
            .subscribe({
                delGPS()
                btnAddGPS.isEnabled = true
            }, { it.printStackTrace() })
    }

    private fun addGPS() {
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE)
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)

        val permissions = mutableListOf(PERMISSION_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(PERMISSION_BACKGROUND_LOCATION)
        }
        if (hasAllPermissions(permissions)) {
            startMockingLocation()
        } else {
            geoPermission.launch(permissions.toTypedArray())
        }
    }

    private fun delGPS() = locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        .also {
            mockDisposable.clear()
        }.also {
            lat = 55.75578
            lon = 37.61786
        }

    @SuppressLint("MissingPermission")
    private fun startMockingLocation() {
        mockDisposable += Observable.interval(FAKE_GEOLOCATION_INTERVAL, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .map {
                Location(LocationManager.GPS_PROVIDER)
                    .apply {
                        lat += 0.0005
                        lon += 0.0005
                        latitude = lat
                        longitude = lon
                        time = System.currentTimeMillis()
                        accuracy = 25f
                        elapsedRealtimeNanos = System.nanoTime()
                    }
            }
            .subscribe { newLocation ->
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation)
//                    LocationServices.getFusedLocationProviderClient(this).setMockMode(true)
//                    LocationServices.getFusedLocationProviderClient(this).setMockLocation(newLocation)
            }
    }
}

fun Observable<Unit>.throttleFirst(skipDurationMillis: Long = 500L): Observable<Unit> = compose { it.throttleFirst(skipDurationMillis, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }
fun Activity.hasAllPermissions(permissions: List<String>): Boolean = permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
