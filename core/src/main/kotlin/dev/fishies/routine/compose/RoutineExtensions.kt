package dev.fishies.routine.compose

import dev.fishies.routine.RoutineBuilder
import dev.fishies.routine.wait
import kotlin.time.Duration

/**
 * Interrupt this command after the given [duration].
 */
fun RoutineBuilder.timeout(duration: Duration) = deadline(wait(duration), this)