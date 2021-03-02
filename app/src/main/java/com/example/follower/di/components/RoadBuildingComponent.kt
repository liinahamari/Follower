package com.example.follower.di.components

import com.example.follower.di.modules.RoadBuildingModule
import com.example.follower.screens.settings.SettingsScope
import com.example.follower.screens.trace_map.TraceFragment
import dagger.Subcomponent

@SettingsScope
@Subcomponent(modules = [RoadBuildingModule::class])
interface RoadBuildingComponent {
    fun inject(fragment: TraceFragment)
}