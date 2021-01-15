package com.example.follower

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import com.example.follower.di.AppComponent
import com.example.follower.di.DaggerAppComponent
import com.example.follower.model.PersistedLocaleResult
import com.example.follower.model.PreferencesRepository
import com.example.follower.services.CHANNEL_ID
import es.dmoral.toasty.Toasty
import java.util.*
import javax.inject.Inject

class FollowerApp: MultiDexApplication() {
    @Inject lateinit var preferencesRepository: PreferencesRepository
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        setupDagger()
        super.onCreate()
        INSTANCE = this

        Toasty.Config.getInstance().apply()

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))
    }

    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale(defaultLocale = Locale.getDefault().language))

    override fun onConfigurationChanged(newConfig: Configuration) {
        preferencesRepository.getPersistedLocale()
            .blockingGet().also {
                if (it is PersistedLocaleResult.Success) {
                    Locale.setDefault(it.locale)
                    newConfig.setLocale(it.locale)
                }
            }
        super.onConfigurationChanged(newConfig)
    }

    private fun setupDagger() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    companion object {
        lateinit var INSTANCE: FollowerApp
    }
}