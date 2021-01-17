package com.example.follower.screens.tracking_control

import com.example.follower.base.BaseViewModel
import com.example.follower.interactors.BaseActivitySettingsInteractor
import com.example.follower.screens.map.WayPoint
import javax.inject.Inject

class TrackingControlViewModel @Inject constructor(private val prefInteractor: BaseActivitySettingsInteractor) : BaseViewModel() {
    fun saveTrack(title: String, wayPoints: List<WayPoint>) {

    }
}