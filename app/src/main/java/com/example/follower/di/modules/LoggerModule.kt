package com.example.follower.di.modules

import android.content.Context
import com.example.follower.helper.FlightRecorder
import com.example.follower.helper.rx.BaseComposers
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

/** related to file_paths.xml -> TempLogs directory*/
private const val DEBUG_LOGS_DIR = "TempLogs"
const val DEBUG_LOGS_STORAGE_FILE = "tape.log"

@Module
class LoggerModule {
    @Provides
    @Singleton
    fun provideLogger(@Named(DEBUG_LOGS_STORAGE_FILE) file: File, baseComposers: BaseComposers): FlightRecorder = FlightRecorder(file, baseComposers)

    @Provides
    @Singleton
    @Named(DEBUG_LOGS_STORAGE_FILE)
    fun provideLogFile(context: Context): File = File(File(context.filesDir, DEBUG_LOGS_DIR).apply {
        if (exists().not()) {
            mkdir()
        }
    }, DEBUG_LOGS_STORAGE_FILE)
}