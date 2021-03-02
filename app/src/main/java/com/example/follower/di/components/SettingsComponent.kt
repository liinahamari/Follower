package com.example.follower.di.components

import com.example.follower.di.modules.SettingsModule
import com.example.follower.screens.settings.SettingsScope
import com.example.follower.screens.settings.SettingsFragment
import dagger.Subcomponent

@SettingsScope
@Subcomponent(modules = [SettingsModule::class])
interface SettingsComponent {
    fun inject(fragment: SettingsFragment)
}