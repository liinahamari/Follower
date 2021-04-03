package com.example.follower.screens.logs

sealed class LogUi {
    abstract val time: Long
    abstract val thread: String

    data class InfoLog(val message: String, override val time: Long, override val thread: String) : LogUi()
    data class ErrorLog(val label: String, val stacktrace: String, override val time: Long, override val thread: String) : LogUi()
}