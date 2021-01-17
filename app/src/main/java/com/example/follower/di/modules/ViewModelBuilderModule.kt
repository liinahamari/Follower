package com.example.follower.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.follower.base.BaseActivity
import com.example.follower.di.ViewModelFactory
import com.example.follower.di.ViewModelKey
import com.example.follower.screens.map.MapFragmentViewModel
import com.example.follower.screens.track_list.TrackListViewModel
import com.example.follower.screens.tracking_control.TrackingControlViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelBuilderModule {
    @Binds
    @IntoMap
    @ViewModelKey(BaseActivity.BaseActivityViewModel::class)
    abstract fun bindBaseViewModel(viewModel: BaseActivity.BaseActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrackingControlViewModel::class)
    abstract fun trackingControlViewModel(viewModel: TrackingControlViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrackListViewModel::class)
    abstract fun trackListViewModel(viewModel: TrackListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapFragmentViewModel::class)
    abstract fun mapFragmentViewModel(viewModel: MapFragmentViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
