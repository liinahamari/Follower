package com.example.follower.screens.show_trace

import android.content.Context
import android.location.Geocoder
import dagger.Module
import dagger.Provides
import java.util.*

@Module
class ShowTraceModule {
    @Provides
    @ShowTraceScope
    fun provideGeocoder(context: Context): Geocoder = Geocoder(context, Locale.UK)

    @Provides
    @ShowTraceScope
    fun provideMapper(geocoder: Geocoder): AddressMapper = AddressMapper(geocoder)
}