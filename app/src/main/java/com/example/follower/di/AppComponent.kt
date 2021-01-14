package com.example.follower.di

import com.example.follower.LocationTrackingService
import com.example.follower.FollowerApp
import com.example.follower.MainActivity
import com.example.follower.screens.show_trace.ShowTraceActivity
import com.example.follower.screens.show_trace.ShowTraceComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, LoggerModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: FollowerApp): Builder
        fun build(): AppComponent
    }

    fun showTraceComponent(): ShowTraceComponent.Builder

    fun inject(service: LocationTrackingService)
}