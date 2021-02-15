package com.example.follower.screens.logs

sealed class LogUi {
    data class InfoLog(val message: String) : LogUi()
    data class ErrorLog(val label: String, val stacktrace: List<String>) : LogUi()
}