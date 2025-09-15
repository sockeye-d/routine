package dev.fishies.routine

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val progressBarChars = listOf(" ", "▁", "▂", "▃", "▄", "▅", "▆", "▇", "█")

/**
 * A [routine] that waits for [duration], then exits.
 */
@Suppress("DefaultLocale")
fun wait(duration: Duration) = routine(typeName = "wait") {
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

/**
 * A [routine] that waits until [condition] is false.
 */
fun waitUntil(condition: () -> Boolean) = routine(typeName = "waitUntil") {
    ready()
    display = { "$name ($condition)" }
    yieldWhile({ condition() }) {}
}

/**
 * The [Unit] routine — e.g., a routine that does nothing, also known as a /no-?op/.
 */
fun unit() = routine { ready() }
