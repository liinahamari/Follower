package dev.liinahamari.follower.di.components

import dev.liinahamari.follower.di.modules.BiometricModule
import dev.liinahamari.follower.di.modules.BiometricScope
import dev.liinahamari.follower.di.modules.SettingsModule
import dev.liinahamari.follower.screens.track_list.TrackListFragment
import dagger.Subcomponent

@BiometricScope
@Subcomponent(modules = [BiometricModule::class])
interface BiometricComponent {
    fun inject(fragment: TrackListFragment)

    fun settingsComponent(module: SettingsModule): SettingsComponent
}