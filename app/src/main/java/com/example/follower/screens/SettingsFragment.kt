package com.example.follower.screens

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.follower.R

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = setPreferencesFromResource(R.xml.preferences, rootKey)
}