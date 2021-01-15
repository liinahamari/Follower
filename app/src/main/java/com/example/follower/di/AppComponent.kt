package com.example.follower.di

import com.example.follower.FollowerApp
import com.example.follower.screens.show_trace.ShowTraceComponent
import com.example.follower.screens.tracking_control.TrackingControlFragment
import com.example.follower.services.LocationTrackingService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, LoggerModule::class, ViewModelBuilderModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: FollowerApp): Builder
        fun build(): AppComponent
    }

    fun showTraceComponent(): ShowTraceComponent.Builder

    fun inject(fragment: TrackingControlFragment)
    fun inject(service: LocationTrackingService)
}