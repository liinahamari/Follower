/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.follower.di.components

import dagger.BindsInstance
import dagger.Component
import dev.liinahamari.follower.FollowerApp
import dev.liinahamari.follower.base.BaseDialogFragment
import dev.liinahamari.follower.base.BaseFragment
import dev.liinahamari.follower.base.BaseService
import dev.liinahamari.follower.base.NotifyingActivity
import dev.liinahamari.follower.di.modules.AppModule
import dev.liinahamari.follower.di.modules.BiometricModule
import dev.liinahamari.follower.di.modules.DatabaseModule
import dev.liinahamari.follower.di.modules.LoggerModule
import dev.liinahamari.follower.di.modules.NetworkModule
import dev.liinahamari.follower.di.modules.ServiceModule
import dev.liinahamari.follower.di.modules.TrackingControlModule
import dev.liinahamari.follower.di.modules.ViewModelBuilderModule
import dev.liinahamari.follower.receivers.BootReceiver
import dev.liinahamari.follower.screens.RouteActivity
import dev.liinahamari.follower.services.AutoTrackingSchedulingService
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, LoggerModule::class, ViewModelBuilderModule::class, DatabaseModule::class, ServiceModule::class, NetworkModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: FollowerApp): Builder

        /** for testing purposes */
        fun serviceModule(module: ServiceModule): Builder

        /** for testing purposes */
        fun dbModule(module: DatabaseModule): Builder

        fun build(): AppComponent
    }

    fun biometricComponent(module: BiometricModule): BiometricComponent
    fun roadBuildingComponent(): RoadBuildingComponent
    fun trackingControlComponent(module: TrackingControlModule): TrackingControlComponent

    fun inject(app: FollowerApp)

    fun inject(fragment: BaseFragment)
    fun inject(fragment: BaseDialogFragment)

    fun inject(activity: NotifyingActivity)
    fun inject(activity: RouteActivity)

    fun inject(service: BaseService)
    fun inject(service: AutoTrackingSchedulingService)
    fun inject(service: LocationTrackingService)
    fun inject(receiver: BootReceiver)
}
