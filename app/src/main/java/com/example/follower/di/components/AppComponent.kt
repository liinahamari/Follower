package com.example.follower.di.components

import com.example.follower.FollowerApp
import com.example.follower.base.BaseFragment
import com.example.follower.di.modules.*
import com.example.follower.screens.MainActivity
import com.example.follower.screens.logs.LogsFragment
import com.example.follower.services.LocationTrackingService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, LoggerModule::class, ViewModelBuilderModule::class, DatabaseModule::class, ServiceModule::class, NetworkModule::class, WorkersBindingModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: FollowerApp): Builder
        fun serviceModule(module: ServiceModule): Builder
        fun build(): AppComponent
    }

    fun biometricComponent(module: BiometricModule): BiometricComponent
    fun roadBuildingComponent(): RoadBuildingComponent
    fun trackingControlComponent(module: TrackingControlModule): TrackingControlComponent

    fun inject(fragment: BaseFragment)
    fun inject(fragment: LogsFragment)
    fun inject(app: FollowerApp)
    fun inject(service: LocationTrackingService)
    fun inject(activity: MainActivity)
}