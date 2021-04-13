package dev.liinahamari.follower.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.liinahamari.follower.di.ViewModelFactory
import dev.liinahamari.follower.di.ViewModelKey
import dev.liinahamari.follower.screens.address_trace.AddressTraceViewModel
import dev.liinahamari.follower.screens.logs.LogsFragmentViewModel
import dev.liinahamari.follower.screens.track_list.TrackListViewModel
import dev.liinahamari.follower.screens.tracking_control.TrackingControlViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dev.liinahamari.follower.base.MapFragment
import dev.liinahamari.follower.screens.MainActivity
import dev.liinahamari.follower.screens.track_list.FtpSharingViewModel

@Module
abstract class ViewModelBuilderModule {
    @Binds
    @IntoMap
    @ViewModelKey(LogsFragmentViewModel::class)
    abstract fun logsViewModel(viewModel: LogsFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivity.MainActivityViewModel::class)
    abstract fun mainActivityViewModel(viewModel: MainActivity.MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapFragment.MapFragmentViewModel::class)
    abstract fun mapFragmentViewModel(viewModel: MapFragment.MapFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FtpSharingViewModel::class)
    abstract fun ftpSharingViewModel(viewModel: FtpSharingViewModel): ViewModel

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
