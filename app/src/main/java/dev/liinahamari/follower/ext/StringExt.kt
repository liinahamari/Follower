package dev.liinahamari.follower.ext

import dev.liinahamari.follower.helper.FlightRecorder
import dev.liinahamari.follower.helper.SEPARATOR

fun String.yellow() = 27.toChar() + "[33m$this" + 27.toChar() + "[0m"
fun String.red() = 27.toChar() + "[31m$this" + 27.toChar() + "[0m"

fun String.toLogMessage(priority: FlightRecorder.Priority) = "${FlightRecorder.getPriorityPattern(priority)}  ${now()} $SEPARATOR${Thread.currentThread().name}$SEPARATOR: $this\n\n"
