package dev.liinahamari.follower.di.components

import dev.liinahamari.follower.di.modules.SettingsModule
import dev.liinahamari.follower.screens.settings.SettingsScope
import dev.liinahamari.follower.screens.settings.SettingsFragment
import dagger.Subcomponent

@SettingsScope
@Subcomponent(modules = [SettingsModule::class])
interface SettingsComponent {
    fun inject(fragment: SettingsFragment)
}