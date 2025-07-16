package org.fishnpotatoes.routine

import kotlin.Any
import kotlin.collections.filter


private typealias Action = () -> Unit

/**
 * The thing that runs the [Routine]s
 */
object RoutineManager {
    internal val routines = mutableListOf<RoutineBuilder>()
    internal val locks = mutableMapOf<Any, RoutineBuilder>()
    internal val deferredActions = mutableListOf<Action>()

    val hasCommands
        get() = routines.isNotEmpty()

    fun tick(): Boolean {
        // runs twice because you want all routines added before ticking to get added,
        // and you also want all the commands that can be removed to be removed as soon as possible
        runDeferred()
        for (routine in routines) {
            routine.runSingleStep()
            if (routine.finished) {
                defer {
                    routines.remove(routine)
                }
            }
        }
        runDeferred()
        return hasCommands
    }

    private fun runDeferred() {
        deferredActions.forEach(Action::invoke)
        deferredActions.clear()
    }

    internal fun defer(block: Action) = deferredActions.add(block)
    internal fun lock(routine: RoutineBuilder) {
        locks.putAll(routine.locks.map { it to routine })
    }

    internal fun unlock(routine: RoutineBuilder) {
        routine.locks.forEach(locks::remove)
    }

    internal fun conflictingRoutines(locks: Set<Any>) =
        locks.filter { it in RoutineManager.locks.keys }.map { RoutineManager.locks[it]!! }.toSet()
}

fun RoutineBuilder.run(interruptOther: Boolean = true) = RoutineManager.defer {
    val conflicts = RoutineManager.conflictingRoutines(locks)
    if (!interruptOther && conflicts.isEmpty()) {
        return@defer
    }

    for (conflict in conflicts) {
        conflict.interrupt()
    }

    RoutineManager.routines.add(this)
    RoutineManager.lock(this)
}

fun RoutineBuilder.interrupt() {
    interruptRoutine()
    RoutineManager.routines.remove(this)
    RoutineManager.unlock(this)
}