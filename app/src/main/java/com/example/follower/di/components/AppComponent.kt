package com.example.follower.di.components

import com.example.follower.FollowerApp
import com.example.follower.screens.logs.LogsActivity
import com.example.follower.screens.MainActivity
import com.example.follower.base.BaseActivity
import com.example.follower.di.modules.AppModule
import com.example.follower.di.modules.DatabaseModule
import com.example.follower.di.modules.LoggerModule
import com.example.follower.di.modules.ServiceModule
import com.example.follower.di.modules.ViewModelBuilderModule
import com.example.follower.screens.map.MapFragment
import com.example.follower.screens.show_trace.ShowTraceComponent
import com.example.follower.screens.track_list.TrackListFragment
import com.example.follower.screens.tracking_control.TrackingControlFragment
import com.example.follower.services.LocationTrackingService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, LoggerModule::class, ViewModelBuilderModule::class, DatabaseModule::class, ServiceModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: FollowerApp): Builder
        fun build(): AppComponent
    }

    fun showTraceComponent(): ShowTraceComponent.Builder

    fun inject(fragment: TrackingControlFragment)
    fun inject(fragment: TrackListFragment)
    fun inject(app: FollowerApp)
    fun inject(fragment: MapFragment)
    fun inject(service: LocationTrackingService)
    fun inject(activity: BaseActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: LogsActivity)
}