package dev.fishies.routine.compose

import dev.fishies.routine.*
import dev.fishies.routine.RoutineManager.interrupt
import dev.fishies.routine.util.indexRange
import java.util.Collections

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
 * On interrupt, all still running routines are interrupted as well.
 */
fun parallel(vararg routines: RoutineBuilder, name: String? = null, setup: Routine.() -> Unit = {}) =
    routine(name = name, typeName = "parallel") {
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

/**
 * Run a series of routines.
 *
 * On interrupt, the currently running routine is interrupted.
 */
fun serial(vararg routines: RoutineBuilder, name: String? = null, setup: Routine.() -> Unit = {}) =
    routine(name = name, typeName = "serial") {
        var current = 0
        requiresAll(routines.flatMap { it.locks })
        display = { groupDisplayString(this, routines) { i, _ -> if (i == current) ">" else "." } }
        setup()
        ready()
        yieldWhile({ current < routines.size }) {
            routines[current].runSingleStep()
            if (routines[current].finished) current++
        }

        if (current in routines.indexRange) {
            routines[current].interrupt()
        }
    }

/**
 * "Deadline" a set of routines against another routine.
 *
 * This is effectively a [parallel] group, but all the routines that aren't finished when the deadline routine is
 * finished get interrupted.
 */
fun deadline(
    deadline: RoutineBuilder,
    vararg routines: RoutineBuilder,
    name: String? = null,
    setup: Routine.() -> Unit = {},
) = routine(name = name, typeName = "deadline") {
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
