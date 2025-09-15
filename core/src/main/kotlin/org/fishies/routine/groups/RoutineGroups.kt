package org.fishies.routine.groups

import org.fishies.routine.*
import org.fishies.routine.util.indexRange
import java.util.Collections
import kotlin.time.Duration

/**
 * @suppress
 */
fun groupDisplayString(
    groupedRoutine: Routine,
    routines: Array<out RoutineBuilder>,
    prefix: (Int, RoutineBuilder) -> String = { _, _ -> "" },
) = """${groupedRoutine.name}
${routines.mapIndexed { i, rt -> rt.toString() }.joinToString("\n").prependIndent(RoutineManager.INDENT)}
""".trimMargin()

private val Array<out RoutineBuilder>.onlyFinished
    get() = this.filter { it.finished }

private val Array<out RoutineBuilder>.onlyUnfinished
    get() = this.filter { !it.finished }

/**
 * Run a set of routines in parallel.
 *
 * On interrupt, all
 */
fun parallel(vararg routines: RoutineBuilder, setup: Routine.() -> Unit = {}) = routine {
    typeName = "parallel"
    for (routine in routines) {
        require(Collections.disjoint(routine.locks, locks)) {
            "Commands in a parallel group cannot share requirements"
        }
        requiresAll(routine.locks)
    }
    display = { groupDisplayString(this, routines) { i, rt -> if (!rt.finished) ">" else "." } }
    setup()
    ready()
    yieldWhile({ !routines.all(Routine::finished) }) {
        for (routine in routines.onlyUnfinished) {
            routine.runSingleStep()
        }
    }
    for (routine in routines.onlyUnfinished) {
        routine.interrupt()
    }
}

fun serial(vararg routines: RoutineBuilder, setup: Routine.() -> Unit = {}) = routine {
    typeName = "serial"
    var current = 0
    requiresAll(routines.flatMap { it.locks })
    display = { groupDisplayString(this, routines) { i, _ -> if (i == current) ">" else "." } }
    setup()
    ready()
    while (current < routines.size) {
        routines[current].runSingleStep()

        if (routines[current].finished) {
            current++
        }

        if (yield()) {
            break
        }
    }

    if (current in routines.indexRange) {
        routines[current].interrupt()
    }
}

fun deadline(deadline: RoutineBuilder, vararg routines: RoutineBuilder, setup: Routine.() -> Unit = {}) = routine {
    typeName = "deadline"
    for (routine in routines) {
        require(Collections.disjoint(routine.locks, locks)) {
            "Commands in a deadline parallel group cannot share requirements"
        }
        requiresAll(routine.locks)
    }
    display = { groupDisplayString(this, arrayOf(deadline, *routines)) { i, rt -> if (!rt.finished) ">" else "." } }
    setup()
    ready()
    yieldWhile({ !deadline.finished && routines.onlyUnfinished.isNotEmpty() }) {
        deadline.runSingleStep()
        for (routine in routines.onlyUnfinished) {
            routine.runSingleStep()
        }
    }

    for (routine in routines.onlyUnfinished) {
        routine.interrupt()
    }
}

fun RoutineBuilder.timeout(duration: Duration) = deadline(wait(duration), this)