package dev.liinahamari.follower.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dev.liinahamari.follower.ext.createFileIfNotExist
import dev.liinahamari.follower.helper.FlightRecorder
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
    fun provideLogFile(@Named(APP_CONTEXT) context: Context): File = context.createFileIfNotExist(DEBUG_LOGS_STORAGE_FILE, DEBUG_LOGS_DIR)
}