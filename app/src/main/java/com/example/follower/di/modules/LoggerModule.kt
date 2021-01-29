package com.example.follower.di.modules

import android.content.Context
import com.example.follower.helper.FlightRecorder
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

/** related to file_paths.xml -> TempLogs directory*/
private const val DEBUG_LOGS_DIR = "TempLogs"
const val DEBUG_LOGS_STORAGE_FILE_NAME = "tape.log"

@Module
class LoggerModule {
    @Provides
    @Singleton
    fun provideLogger(@Named(DEBUG_LOGS_STORAGE_FILE_NAME) file: File): FlightRecorder = FlightRecorder(file)

    @Provides
    @Singleton
    @Named(DEBUG_LOGS_STORAGE_FILE_NAME)
    fun provideLogFile(context: Context): File = File(File(context.filesDir, DEBUG_LOGS_DIR).apply {
        if (exists().not()) {
            mkdir()
        }
    }, DEBUG_LOGS_STORAGE_FILE_NAME)
}