package dev.liinahamari.follower.di.components

import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.base.BaseFragment
import dev.liinahamari.follower.screens.MainActivity
import dev.liinahamari.follower.screens.logs.LogsFragment
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import dagger.BindsInstance
import dagger.Component
import dev.liinahamari.follower.base.BaseDialogFragment
import dev.liinahamari.follower.base.BaseService
import dev.liinahamari.follower.di.modules.*
import dev.liinahamari.follower.services.AutoTrackingSchedulingService
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, LoggerModule::class, ViewModelBuilderModule::class, DatabaseModule::class, ServiceModule::class, NetworkModule::class, WorkersBindingModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: FollowerApp): Builder

        fun serviceModule(module: ServiceModule): Builder

        /** for testing purposes */
        fun dbModule(module: DatabaseModule): Builder

        /** for testing purposes */

        fun build(): AppComponent
    }

    fun biometricComponent(module: BiometricModule): BiometricComponent
    fun roadBuildingComponent(): RoadBuildingComponent
    fun trackingControlComponent(module: TrackingControlModule): TrackingControlComponent

    fun inject(service: BaseService)
    fun inject(fragment: BaseFragment)
    fun inject(fragment: LogsFragment)
    fun inject(fragment: BaseDialogFragment)
    fun inject(app: FollowerApp)
    fun inject(service: LocationTrackingService)
    fun inject(activity: MainActivity)
    fun inject(service: AutoTrackingSchedulingService)
}