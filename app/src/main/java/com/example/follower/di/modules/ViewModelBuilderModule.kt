package com.example.follower.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.follower.di.ViewModelFactory
import com.example.follower.di.ViewModelKey
import com.example.follower.screens.MainActivity
import com.example.follower.screens.address_trace.AddressTraceViewModel
import com.example.follower.screens.logs.LogsFragmentViewModel
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
    @ViewModelKey(MainActivity.MainActivityViewModel::class)
    abstract fun bindBaseViewModel(viewModel: MainActivity.MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LogsFragmentViewModel::class)
    abstract fun logsViewModel(viewModel: LogsFragmentViewModel): ViewModel

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
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
