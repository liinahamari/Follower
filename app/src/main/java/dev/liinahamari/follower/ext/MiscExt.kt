package dev.liinahamari.follower.ext

fun tryLogging(what: () -> Unit) = try { what.invoke() } catch (e: Throwable) { e.printStackTrace() }