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
import dev.liinahamari.follower.screens.track_list.FtpSharingViewModel

@Module
abstract class ViewModelBuilderModule {
    @Binds
    @IntoMap
    @ViewModelKey(LogsFragmentViewModel::class)
    abstract fun logsViewModel(viewModel: LogsFragmentViewModel): ViewModel

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
