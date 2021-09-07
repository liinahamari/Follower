/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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
import dev.liinahamari.follower.model.PreferencesRepository
import dev.liinahamari.follower.screens.crash_screen.CrashStackTraceActivity
import dev.liinahamari.follower.services.AutoTrackingSchedulingService
import dev.liinahamari.follower.services.location_tracking.LocationTrackingService
import dev.liinahamari.loggy_sdk.Loggy
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.createFileIfNotExist
import io.reactivex.rxjava3.internal.functions.Functions
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import net.gotev.uploadservice.UploadServiceConfig
import org.acra.*
import org.acra.annotation.*
import org.acra.data.StringFormat
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.system.exitProcess

private const val DEBUG_LOGS_DIR = "TempLogs"
private const val DEBUG_LOGS_STORAGE_FILE = "tape.log"
private const val USER_ID = "dummy_id"
private const val FTP_FILE_UPLOAD_SERVICE_ID = "FTP_FILE_UPLOAD_SERVICE_ID"
private const val MY_EMAIL = "me@liinahamari.dev"

/*TODO: test PROD with R8 enabled*/
@AcraCore(buildConfigClass = BuildConfig::class, reportFormat = StringFormat.JSON)
/*
@AcraHttpSender(uri = "http://yourserver.com/yourscript",
    basicAuthLogin = "  yourlogin", // optional
    basicAuthPassword = "y0uRpa$\$w0rd", // optional
    httpMethod = HttpSender.Method.POST)
*/
@AcraToast(resText = R.string.acra_toast_text, length = Toast.LENGTH_LONG)
@AcraScheduler(requiresNetworkType = JobInfo.NETWORK_TYPE_ANY/*fixme: debug/prod distinction*/, requiresBatteryNotLow = true)
@AcraMailSender(mailTo = MY_EMAIL) /*FIXME: temporary solution*/
class FollowerApp : Application() {
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var workerFactory: WorkerFactory
    @Inject lateinit var notificationManager: NotificationManager

    lateinit var appComponent: AppComponent
    private val anrWatchDog = ANRWatchDog(2000)

    override fun onCreate() {
        setupDagger()
        super.onCreate()
        Loggy.init(
            application = this,
            logFile = createFileIfNotExist(DEBUG_LOGS_STORAGE_FILE, DEBUG_LOGS_DIR),
            integratorEmail = MY_EMAIL,
            userId = USER_ID
        )
        preferencesRepository.incrementAppLaunchCounter()
        setupWorkManager()
        setupAnrWatchDog()
        preferencesRepository.applyDefaultPreferences()
        setupOsmdroid()
        setupNotificationChannels()
        setupFtpUploadingService()
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())
        setupCrashStackTraceScreen()
    }

    private fun setupCrashStackTraceScreen() = Thread.setDefaultUncaughtExceptionHandler { thread, error ->
        FlightRecorder.e("App Crash catch", error, false)

        if (BuildConfig.DEBUG) {
            try {
                startActivity(CrashStackTraceActivity.newIntent(this, thread.name, error))
            } catch (e: Exception) {
                FlightRecorder.e("UncaughtExceptionHandler", e)
            } finally {
                exitProcess(1)
            }
        }
    }

    private fun setupFtpUploadingService() = UploadServiceConfig.initialize(
        context = this,
        defaultNotificationChannel = FTP_FILE_UPLOAD_SERVICE_ID,
        debug = BuildConfig.DEBUG
    )

    private fun setupNotificationChannels() {
        //todo strings
        notificationManager.createNotificationChannel(NotificationChannel(AutoTrackingSchedulingService.CHANNEL_ID, "Auto tracking scheduling", NotificationManager.IMPORTANCE_DEFAULT))
        notificationManager.createNotificationChannel(NotificationChannel(LocationTrackingService.CHANNEL_ID, "GPS tracker", NotificationManager.IMPORTANCE_LOW))
        notificationManager.createNotificationChannel(NotificationChannel(FTP_FILE_UPLOAD_SERVICE_ID, "FTP file uploading", NotificationManager.IMPORTANCE_LOW))
    }

    private fun setupWorkManager() = WorkManager.initialize(
        applicationContext,
        androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(INFO)
            .setExecutor(Executors.newSingleThreadExecutor())
            .build()
    )

    private fun setupAnrWatchDog() = anrWatchDog
        .setANRListener { error ->
            error?.cause?.let { FlightRecorder.e("ANR $error", error = it) }
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

    /** Blocking device-wide language changes */
    override fun onConfigurationChanged(newConfig: Configuration) {
        preferencesRepository.getPersistedLocale()
            .blockingGet().also {
                Locale.setDefault(it)
                newConfig.setLocale(it)
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