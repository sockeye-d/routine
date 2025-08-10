package org.fishnpotatoes.routine.groups

import org.fishnpotatoes.routine.routine
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.TimeSource

private val progressBarChars = listOf(" ", "▁", "▂", "▃", "▄", "▅", "▆", "▇", "█")

fun wait(duration: Duration) = routine {
    ready()
    val start = TimeSource.Monotonic.markNow()
    display = { "$name ${progressBarChars[(start.elapsedNow() / duration * progressBarChars.size).toInt()]}" }
    while (start.elapsedNow() < duration) {
        if (yield()) break
    }
}

fun waitUntil(condition: () -> Boolean) = routine {
    ready()
    while (condition()) if (yield()) break
}

fun unit() = routine { ready() }
