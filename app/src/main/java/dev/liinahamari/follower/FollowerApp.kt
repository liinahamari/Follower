@file:Suppress("NonConstantResourceId")
package dev.liinahamari.follower

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import dev.liinahamari.follower.di.components.AppComponent
import dev.liinahamari.follower.di.components.DaggerAppComponent
import dev.liinahamari.follower.ext.provideUpdatedContextWithNewLocale
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.model.PersistedLocaleResult
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.screens.logs.MY_EMAIL
import dev.liinahamari.follower.services.CHANNEL_ID
import com.github.anrwatchdog.ANRWatchDog
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins
import org.acra.*
import org.acra.annotation.*
import org.acra.data.StringFormat
import java.util.*
import javax.inject.Inject

/*TODO: test PROD with R8 enabled*/
@AcraCore(buildConfigClass = BuildConfig::class, reportFormat = StringFormat.JSON)
/*
@AcraHttpSender(uri = "http://yourserver.com/yourscript",
    basicAuthLogin = "yourlogin", // optional
    basicAuthPassword = "y0uRpa$\$w0rd", // optional
    httpMethod = HttpSender.Method.POST)
*/
@AcraToast(resText=R.string.acra_toast_text, length = Toast.LENGTH_LONG)
@AcraScheduler(requiresNetworkType = JobInfo.NETWORK_TYPE_ANY/*fixme: debug/prod distinction*/, requiresBatteryNotLow = true)
@AcraMailSender(mailTo = MY_EMAIL) /*FIXME: temporary solution*/
class FollowerApp : Application() {
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var logger: FlightRecorder
    @Inject lateinit var notificationManager: NotificationManager
    lateinit var appComponent: AppComponent
    private val anrWatchDog = ANRWatchDog(2000)

    override fun onCreate() {
        setupDagger()
        super.onCreate()

        setupAnrWatchDog()

        preferencesRepository.applyDefaultPreferences().blockingAwait() /*todo: to splash screen?*/

        setupOsmdroid()

        notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))

        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())
    }

    private fun setupAnrWatchDog() = anrWatchDog
        .setANRListener { error ->
            logger.wtf { "ANR: $error" }
            logger.e("ANR", stackTrace = error.cause?.stackTrace ?: emptyArray())
        }.also {
            it.start()
        }

    private fun setupOsmdroid() {
        with(org.osmdroid.config.Configuration.getInstance()) {
            /*set user agent to prevent getting banned from the OSM servers*/
            userAgentValue = BuildConfig.APPLICATION_ID
            /*set the path for osmdroid's files (for example, tile cache)*/
            osmdroidBasePath = getExternalFilesDir(null)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.provideUpdatedContextWithNewLocale(defaultLocale = Locale.getDefault().language))
        ACRA.init(this)
    }

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

    @VisibleForTesting
    fun setupDagger(
        appComponent: AppComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    ) {
        this.appComponent = appComponent
            .apply { inject(this@FollowerApp) }
    }
}