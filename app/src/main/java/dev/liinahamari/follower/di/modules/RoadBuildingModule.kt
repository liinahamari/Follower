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