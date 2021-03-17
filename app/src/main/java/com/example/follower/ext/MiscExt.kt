package com.example.follower.ext

import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.ExecutionException

fun tryLogging(what: () -> Unit) = try {
    what.invoke()
} catch (e: Throwable) {
    e.printStackTrace()
}

/** pay attention - it's for UNIQUE work!*/
fun WorkManager.isWorkScheduled(tag: String): Boolean = try {
    getWorkInfosForUniqueWork(tag).get()
        .map { it.state }
        .any { it == WorkInfo.State.RUNNING || it == WorkInfo.State.ENQUEUED }
} catch (e: ExecutionException) {
    e.printStackTrace()
    false
} catch (e: InterruptedException) {
    e.printStackTrace()
    false
}