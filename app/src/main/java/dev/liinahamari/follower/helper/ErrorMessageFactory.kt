package dev.liinahamari.follower.helper

import android.content.Context
import dev.liinahamari.follower.R
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorMessageFactory @Inject constructor(private val context: Context) {
    fun errorMessage(t: Throwable): String = when (t) {
        is SocketTimeoutException, is UnknownHostException -> context.resources.getString(R.string.error_no_connection)
        else -> context.resources.getString(R.string.error_unexpected)
    }
}