package org.fishnpotatoes.routine


private typealias Action = () -> Unit

object RoutineManager {
    internal val routines = mutableListOf<RoutineBuilder>()
    internal val required = mutableMapOf<Any, RoutineBuilder>()
    internal val deferredActions = mutableListOf<Action>()

    fun tick() {
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
    }

    private fun runDeferred() {
        deferredActions.forEach(Action::invoke)
        deferredActions.clear()
    }

    internal fun defer(block: Action) = deferredActions.add(block)
    internal fun addRequirements(routine: RoutineBuilder) {
        required.putAll(routine.requirements.map { it to routine })
    }

    internal fun removeRequirements(routine: RoutineBuilder) {
        routine.requirements.forEach(required::remove)
    }

    internal fun conflictingRoutines(requirements: Set<Any>) =
        requirements.filter { it in required.keys }.map { required[it]!! }.toSet()
}

fun RoutineBuilder.run(interruptOther: Boolean = true) = RoutineManager.defer {
    val conflicts = RoutineManager.conflictingRoutines(requirements)
    if (!interruptOther && conflicts.isEmpty()) {
        return@defer
    }

    for (conflict in conflicts) {
        conflict.interrupt()
    }

    RoutineManager.routines.add(this)
    RoutineManager.addRequirements(this)
}

fun RoutineBuilder.interrupt() {
    interruptRoutine()
    RoutineManager.routines.remove(this)
    RoutineManager.removeRequirements(this)
}