package org.fishnpotatoes.routine.groups

import org.fishnpotatoes.routine.*
import java.util.Collections

fun parallel(vararg routines: RoutineBuilder, setup: Routine.() -> Unit = {}) = routine {
    for (routine in routines) {
        require(Collections.disjoint(routine.locks, locks)) {
            "Commands in a parallel group cannot share requirements"
        }
        requires(*routine.locks.toTypedArray())
    }
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
    for (routine in routines) {
        requires(routine.locks)
    }
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

fun deadline(deadlineRoutine: RoutineBuilder, vararg routines: RoutineBuilder, setup: Routine.() -> Unit = {}) = routine {
    for (routine in routines) {
        require(Collections.disjoint(routine.locks, locks)) {
            "Commands in a deadline parallel group cannot share requirements"
        }
        requires(*routine.locks.toTypedArray())
    }
    setup()
    ready()
    while (!deadlineRoutine.finished) {
        deadlineRoutine.runSingleStep()
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