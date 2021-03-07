package com.example.follower.di.components

import com.example.follower.di.modules.TrackingControlModule
import com.example.follower.screens.tracking_control.TrackingControlFragment
import com.example.follower.screens.tracking_control.TrackingControlScope
import dagger.Subcomponent

@TrackingControlScope
@Subcomponent(modules = [TrackingControlModule::class])
interface TrackingControlComponent {
    fun inject(fragment: TrackingControlFragment)
}