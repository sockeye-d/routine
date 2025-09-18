package dev.fishies.routine.compose

import dev.fishies.routine.Routine
import dev.fishies.routine.RoutineScope
import dev.fishies.routine.util.Timer
import dev.fishies.routine.wait
import dev.fishies.routine.waitUntil
import kotlin.time.Duration

/**
 * Interrupt this command after the given [duration].
 */
fun Routine.timeout(duration: Duration) = deadline(wait(duration), this)

/**
 * Interrupt this command once [condition] is true.
 */
fun Routine.stopWhen(condition: () -> Boolean) = deadline(waitUntil(condition), this)

suspend fun RoutineScope.delay(duration: Duration) {
    val timer = Timer()
    while (timer.elapsed < duration) {
        yield()
    }
}
