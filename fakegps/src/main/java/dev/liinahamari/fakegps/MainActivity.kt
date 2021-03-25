package dev.liinahamari.fakegps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

private const val PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
private const val CODE_PERMISSION_LOCATION = 101

class MainActivity : AppCompatActivity(R.layout.activity_main) {
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

        if (hasPermission(PERMISSION_LOCATION)) {
            startMockingLocation()
        } else {
            requestPermissions(arrayOf(PERMISSION_LOCATION), CODE_PERMISSION_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_PERMISSION_LOCATION) {
            handleUsersReactionToPermission(
                permissionToHandle = PERMISSION_LOCATION,
                allPermissions = permissions,
                doIfAllowed = { startMockingLocation() },
                doIfDenied = { throw RuntimeException() },
                doIfNeverAskAgain = { throw RuntimeException() }
            )
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
        mockDisposable += Observable.interval(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
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

fun Activity.handleUsersReactionToPermission(permissionToHandle: String, allPermissions: Array<out String>, doIfDenied: () -> Unit, doIfAllowed: () -> Unit, doIfNeverAskAgain: () -> Unit) {
    if (allPermissions.contains(permissionToHandle)) {
        if (shouldShowRequestPermissionRationale(permissionToHandle)) {
            doIfDenied()
        } else {
            if (hasPermission(permissionToHandle)) {
                doIfAllowed()
            } else {
                doIfNeverAskAgain()
            }
        }
    }
}

fun Activity.hasPermission(permission: String) = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Observable<Unit>.throttleFirst(skipDurationMillis: Long = 500L): Observable<Unit> = compose { it.throttleFirst(skipDurationMillis, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }
