package dev.liinahamari.follower.di.components

import dev.liinahamari.follower.di.modules.RoadBuildingModule
import dev.liinahamari.follower.screens.trace_map.RoadBuildingScope
import dev.liinahamari.follower.screens.trace_map.TrackMapFragment
import dagger.Subcomponent

@RoadBuildingScope
@Subcomponent(modules = [RoadBuildingModule::class])
interface RoadBuildingComponent {
    fun inject(fragment: TrackMapFragment)
}