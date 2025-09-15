package org.fishies.routine.groups

import org.fishies.routine.routine
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val progressBarChars = listOf(" ", "▁", "▂", "▃", "▄", "▅", "▆", "▇", "█")

fun wait(duration: Duration) = routine {
    typeName = "wait"
    ready()
    val start = TimeSource.Monotonic.markNow()
    display = {
        "$name ${
            if (finished) {
                " "
            } else {
                progressBarChars[(start.elapsedNow() / duration * progressBarChars.size).toInt()
                    .coerceIn(0, progressBarChars.size - 1)]
            }
        } (${
            if (finished) {
                String.format("%.1f", duration.toDouble(DurationUnit.SECONDS))
            } else {
                String.format("%.1f", start.elapsedNow().toDouble(DurationUnit.SECONDS))
            }
        } / ${String.format("%.1f", duration.toDouble(DurationUnit.SECONDS))})"
    }
    while (start.elapsedNow() < duration) {
        if (yield()) break
    }
}

fun waitUntil(condition: () -> Boolean) = routine {
    typeName = "waitUntil"
    ready()
    display = { "$name ($condition)" }
    while (condition()) if (yield()) break
}

fun unit() = routine { ready() }
