package com.example.follower.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.follower.base.BaseActivity
import com.example.follower.di.ViewModelFactory
import com.example.follower.di.ViewModelKey
import com.example.follower.screens.address_trace.AddressTraceViewModel
import com.example.follower.screens.logs.LogsActivityViewModel
import com.example.follower.screens.settings.SettingsViewModel
import com.example.follower.screens.trace_map.TraceFragmentViewModel
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
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LogsActivityViewModel::class)
    abstract fun logsViewModel(viewModel: LogsActivityViewModel): ViewModel

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
    @ViewModelKey(AddressTraceViewModel::class)
    abstract fun addressTraceViewModel(viewModel: AddressTraceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TraceFragmentViewModel::class)
    abstract fun mapFragmentViewModel(viewModel: TraceFragmentViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
