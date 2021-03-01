package com.example.follower.di.components

import com.example.follower.di.modules.BiometricModule
import com.example.follower.di.modules.BiometricScope
import com.example.follower.screens.track_list.TrackListFragment
import dagger.Subcomponent

@BiometricScope
@Subcomponent(modules = [BiometricModule::class])
interface BiometricComponent {
    fun inject(fragment: TrackListFragment)

    fun settingsComponent(): SettingsComponent
}