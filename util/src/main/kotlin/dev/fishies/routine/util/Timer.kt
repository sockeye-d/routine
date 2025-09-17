package dev.fishies.routine.util

import kotlin.time.TimeSource

class Timer(private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic) {
    private var now = timeSource.markNow()
    fun reset() {
        now = timeSource.markNow()
    }

    val elapsed = now.elapsedNow()
}