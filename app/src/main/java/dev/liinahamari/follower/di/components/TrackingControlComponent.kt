package dev.liinahamari.follower.di.components

import dev.liinahamari.follower.di.modules.TrackingControlModule
import dev.liinahamari.follower.screens.tracking_control.TrackingControlFragment
import dev.liinahamari.follower.screens.tracking_control.TrackingControlScope
import dagger.Subcomponent

@TrackingControlScope
@Subcomponent(modules = [TrackingControlModule::class])
interface TrackingControlComponent {
    fun inject(fragment: TrackingControlFragment)
}