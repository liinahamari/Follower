package com.example.follower

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import com.example.follower.di.components.AppComponent
import com.example.follower.di.components.DaggerAppComponent
import com.example.follower.ext.provideUpdatedContextWithNewLocale
import com.example.follower.helper.FlightRecorder
import com.example.follower.model.PersistedLocaleResult
import com.example.follower.model.PreferencesRepository
import com.example.follower.services.CHANNEL_ID
import io.reactivex.plugins.RxJavaPlugins
import java.util.*
import javax.inject.Inject

class FollowerApp: Application() {
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var logger: FlightRecorder
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        setupDagger()
        super.onCreate()

        preferencesRepository.applyDefaultPreferences().blockingAwait()

        setupOsmdroid()

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))

        RxJavaPlugins.setErrorHandler { logger.e(label = "GLOBAL", stackTrace = it.stackTrace) }
    }

    private fun setupOsmdroid() {
        with(org.osmdroid.config.Configuration.getInstance()) {
            userAgentValue = BuildConfig.APPLICATION_ID
            osmdroidBasePath = getExternalFilesDir(null)
        }
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
            .apply { inject(this@FollowerApp) }
    }
}