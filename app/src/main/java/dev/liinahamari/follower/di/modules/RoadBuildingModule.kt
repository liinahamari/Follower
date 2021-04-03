package dev.liinahamari.follower.di.modules

import android.content.Context
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.model.TrackDao
import dev.liinahamari.follower.screens.trace_map.RoadBuildingInteractor
import dev.liinahamari.follower.screens.trace_map.RoadBuildingScope
import dagger.Module
import dagger.Provides
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import javax.inject.Named

@Module
class RoadBuildingModule {
    @Provides
    @RoadBuildingScope
    fun provideRoadBuildingInteractor(@Named(APP_CONTEXT) ctx: Context, trackDao: TrackDao, baseComposers: BaseComposers, prefRepo: PreferencesRepository, osmRoadManager: OSRMRoadManager): RoadBuildingInteractor
        = RoadBuildingInteractor(ctx, trackDao, baseComposers, prefRepo, osmRoadManager)

    @Provides
    @RoadBuildingScope
    fun provideOsmRoadManager(@Named(APP_CONTEXT) ctx: Context) = OSRMRoadManager(ctx)
}