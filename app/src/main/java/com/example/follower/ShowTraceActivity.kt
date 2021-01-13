package com.example.follower

import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_show_trace.*
import java.util.*

class ShowTraceActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()
    private val mapper = AddressMapper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_trace)

        disposable += Single.fromCallable {
            FlightRecorder.getEntireRecord()
                .split("\n\n")
                .asSequence()
                .filter { it.contains("Location Changed") }
                .map {
                    val (lat, long) = "Location Changed\\. lat:(\\d+\\.\\d+), long:(\\d+\\.\\d+)".toRegex().find(it)!!.groupValues.drop(1)
                    mapper.transform(lat.toDouble(), long.toDouble())
                }
                .map { it to "(\\w+\\s*\\d*[\\\\/\\w]*\\d*),".toRegex().find(it)!!.groupValues[1] }
                .distinctBy { it.second }
                .map { it.first }
                .joinToString(separator = "\n")
        }
            .doOnError { FlightRecorder.e(stackTrace = it.stackTrace) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                logsContainer.text = it
            })
    }
}

class AddressMapper {
    fun transform(lat: Double, long: Double): String {
        /*
        val addresses: List<Address> = Geocoder(App.INSTANCE, Locale.UK).getFromLocation(lat, long, 1)

        // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        val address: String = addresses[0].getAddressLine(0)
        val city: String = addresses[0].locality
        val state: String = addresses[0].adminArea
        val country: String = addresses[0].countryName
        val postalCode: String? = addresses[0].postalCode
        val knownName: String = addresses[0].featureName
        return "Address: $address; City: $city; State: $state; Country: $country; Postal code: $postalCode; knownName: $knownName\n"
        */
        return Geocoder(FollowerApp.INSTANCE, Locale.UK).getFromLocation(lat, long, 1).first().getAddressLine(0)
    }
}