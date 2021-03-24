package dev.liinahamari.follower.di.components

import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.base.BaseFragment
import dev.liinahamari.follower.screens.MainActivity
import dev.liinahamari.follower.screens.logs.LogsFragment
import dev.liinahamari.follower.services.LocationTrackingService
import dagger.BindsInstance
import dagger.Component
import dev.liinahamari.follower.di.modules.*
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, LoggerModule::class, ViewModelBuilderModule::class, DatabaseModule::class, ServiceModule::class, NetworkModule::class])
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