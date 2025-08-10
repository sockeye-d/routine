package org.fishnpotatoes.routine.groups

import org.fishnpotatoes.routine.*
import java.util.Collections

fun groupDisplayString(
    groupedRoutine: Routine,
    routines: Array<out RoutineBuilder>,
    prefix: (Int, RoutineBuilder) -> String = { _, _ -> "    " },
) = """${groupedRoutine.name} {
    ${routines.mapIndexed { i, it -> prefix(i, it) + it.toString() }.joinToString("\n")}
    }
""".trimMargin()

fun parallel(vararg routines: RoutineBuilder, setup: Routine.() -> Unit = {}) = routine {
    for (routine in routines) {
        require(Collections.disjoint(routine.locks, locks)) {
            "Commands in a parallel group cannot share requirements"
        }
        requiresAll(routine.locks)
    }
    display = { groupDisplayString(this, routines) }
    setup()
    ready()
    while (!routines.all(Routine::finished)) {
        for (routine in routines) {
            if (!routine.finished) routine.runSingleStep()
        }

        if (yield()) {
            for (routine in routines) {
                routine.interrupt()
            }
        }
    }
}

fun sequential(vararg routines: RoutineBuilder, setup: Routine.() -> Unit = {}) = routine {
    var current = 0
    requiresAll(routines.flatMap { it.locks })
    display = { groupDisplayString(this, routines) { i, _ -> if (i == current) "   >" else "    " } }
    setup()
    ready()
    while (current < routines.size) {
        routines[current].runSingleStep()

        if (routines[current].finished) {
            current++
        }

        if (yield()) {
            for (routine in routines) {
                routine.interrupt()
            }
        }
    }
}

fun deadline(deadline: RoutineBuilder, vararg routines: RoutineBuilder, setup: Routine.() -> Unit = {}) = routine {
    for (routine in routines) {
        require(Collections.disjoint(routine.locks, locks)) {
            "Commands in a deadline parallel group cannot share requirements"
        }
        requiresAll(routine.locks)
    }
    setup()
    ready()
    while (!deadline.finished) {
        deadline.runSingleStep()
        for (routine in routines) {
            if (!routine.finished) routine.runSingleStep()
        }

        if (yield()) {
            for (routine in routines) {
                routine.interrupt()
            }
            return@routine
        }
    }

    for (routine in routines) {
        routine.interrupt()
    }
}
