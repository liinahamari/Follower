@file:Suppress("NonConstantResourceId")
package dev.liinahamari.follower

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.content.Context
import android.content.res.Configuration
import android.util.Log.INFO
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.github.anrwatchdog.ANRWatchDog
import dev.liinahamari.follower.di.components.AppComponent
import dev.liinahamari.follower.di.components.DaggerAppComponent
import dev.liinahamari.follower.ext.provideUpdatedContextWithNewLocale
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.model.PersistedLocaleResult
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.screens.logs.MY_EMAIL
import dev.liinahamari.follower.services.AutoTrackingSchedulingService
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins
import net.gotev.uploadservice.UploadServiceConfig
import org.acra.*
import org.acra.annotation.*
import org.acra.data.StringFormat
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

private const val FTP_FILE_UPLOAD_SERVICE_ID = "FTP_FILE_UPLOAD_SERVICE_ID"

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
    @Inject lateinit var workerFactory: WorkerFactory
    @Inject lateinit var logger: FlightRecorder
    @Inject lateinit var notificationManager: NotificationManager
    lateinit var appComponent: AppComponent
    private val anrWatchDog = ANRWatchDog(2000)

    override fun onCreate() {
        setupDagger()
        super.onCreate()
        preferencesRepository.incrementAppLaunchCounter()
        setupWorkManager()
        setupAnrWatchDog()
        preferencesRepository.applyDefaultPreferences().blockingAwait() /*todo: to splash screen?*/
        setupOsmdroid()
        setupNotificationChannels()
        setupFtpUploadingService()
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())
    }

    private fun setupFtpUploadingService() = UploadServiceConfig.initialize(
        context = this,
        defaultNotificationChannel = FTP_FILE_UPLOAD_SERVICE_ID,
        debug = BuildConfig.DEBUG
    )

    private fun setupNotificationChannels() {
        notificationManager.createNotificationChannel(NotificationChannel(AutoTrackingSchedulingService.CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_DEFAULT))
        notificationManager.createNotificationChannel(NotificationChannel(LocationTrackingService.CHANNEL_ID, "Auto tracking scheduling", NotificationManager.IMPORTANCE_DEFAULT))
        notificationManager.createNotificationChannel(NotificationChannel(FTP_FILE_UPLOAD_SERVICE_ID, "FTP file uploading", NotificationManager.IMPORTANCE_LOW))
    }

    private fun setupWorkManager() = WorkManager.initialize(applicationContext,
            androidx.work.Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(INFO)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
        )

    private fun setupAnrWatchDog() = anrWatchDog
        .setANRListener { error ->
            error?.cause?.let { logger.e("ANR $error", error = it) }
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