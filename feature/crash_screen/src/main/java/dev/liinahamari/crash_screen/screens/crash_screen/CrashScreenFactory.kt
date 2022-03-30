/*
 * Copyright 2020-2021 liinahamari
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.liinahamari.crash_screen.screens.crash_screen

import android.app.Application
import android.content.Context
import dev.liinahamari.feature.crash_screen.api.CrashScreenApi
import dev.liinahamari.feature.crash_screen.api.CrashScreenDependencies
import dev.liinahamari.feature.crash_screen.impl.CrashStackTraceActivity
import java.util.logging.Logger
import kotlin.system.exitProcess

object CrashScreenFactory {
    fun createCrashScreenApi(dependencies: CrashScreenDependencies): CrashScreenApi = CrashScreenApi.create(dependencies)
}

object CrashInterceptor {
    @JvmStatic
    fun init(application: Application, logger: Logger = Logger.getGlobal()) {
        Thread.setDefaultUncaughtExceptionHandler(CrashScreenFactory.createCrashScreenApi(object : CrashScreenDependencies {
            override val logger: Logger = logger
            override val context: Context = application
        }).uncaughtExceptionHandler)
    }
}

private fun CrashScreenApi.Companion.create(dependencies: CrashScreenDependencies) = object : CrashScreenApi {
    override val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler = Thread.UncaughtExceptionHandler { t, e ->
        try {
//            logger.log("a")
            dependencies.context.startActivity(CrashStackTraceActivity.newIntent(dependencies.context, t.name, e.stackTraceToString()))
        } catch (e: Exception) {
//            FlightRecorder.e("UncaughtExceptionHandler", e)
        } finally {
            exitProcess(1)
        }
    }
}