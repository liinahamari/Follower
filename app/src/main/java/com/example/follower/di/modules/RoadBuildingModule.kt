package com.example.follower.di.modules

import android.content.Context
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.model.PreferencesRepository
import com.example.follower.model.TrackDao
import com.example.follower.screens.trace_map.RoadBuildingInteractor
import com.example.follower.screens.trace_map.RoadBuildingScope
import dagger.Module
import dagger.Provides
import org.osmdroid.bonuspack.routing.OSRMRoadManager

@Module
class RoadBuildingModule {
    @Provides
    @RoadBuildingScope
    fun provideRoadBuildingInteractor(ctx: Context, trackDao: TrackDao, baseComposers: BaseComposers, prefRepo: PreferencesRepository, osmRoadManager: OSRMRoadManager): RoadBuildingInteractor
        = RoadBuildingInteractor(ctx, trackDao, baseComposers, prefRepo, osmRoadManager)

    @Provides
    @RoadBuildingScope
    fun provideOsmRoadManager(ctx: Context) = OSRMRoadManager(ctx)
}