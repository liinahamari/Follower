package com.example.follower.screens.show_trace

import android.location.Geocoder

@ShowTraceScope
class AddressMapper (private val geocoder: Geocoder) {
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
        return geocoder.getFromLocation(lat, long, 1).first().getAddressLine(0)
    }
}