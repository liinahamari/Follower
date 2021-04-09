package dev.liinahamari.follower.di.modules

import android.content.Context
import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.helper.rx.BaseComposers
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

/** related to file_paths.xml -> TempLogs directory*/
const val DEBUG_LOGS_DIR = "TempLogs"
const val DEBUG_LOGS_STORAGE_FILE = "tape.log"

@Module
class LoggerModule {
    @Provides
    @Singleton
    fun provideLogger(@Named(DEBUG_LOGS_STORAGE_FILE) file: File): FlightRecorder = FlightRecorder(file)

    @Provides
    @Singleton
    @Named(DEBUG_LOGS_STORAGE_FILE)
    fun provideLogFile(@Named(APP_CONTEXT) context: Context): File = File(File(context.filesDir, DEBUG_LOGS_DIR).apply {
        if (exists().not()) {
            mkdir()
        }
    }, DEBUG_LOGS_STORAGE_FILE)
}