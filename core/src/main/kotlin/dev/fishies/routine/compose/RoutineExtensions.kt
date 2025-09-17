package dev.fishies.routine.compose

import dev.fishies.routine.Routine
import dev.fishies.routine.wait
import kotlin.time.Duration

/**
 * Interrupt this command after the given [duration].
 */
fun Routine.timeout(duration: Duration) = deadline(wait(duration), this)