package com.example.follower.di

import android.content.Context
import com.example.follower.FlightRecorder
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

private const val DEBUG_LOGS_DIR = "FlightRecordings"
private const val DEBUG_LOGS_STORAGE_FILE_NAME = "tape.log"

@Module
class LoggerModule {
    @Provides
    @Singleton
    fun provideLogger(file: File): FlightRecorder = FlightRecorder(file)

    @Provides
    @Singleton
    fun provideLogFile(context: Context): File = File(File(context.filesDir, DEBUG_LOGS_DIR).apply {
        if (exists().not()) {
            mkdir()
        }
    }, DEBUG_LOGS_STORAGE_FILE_NAME)
}