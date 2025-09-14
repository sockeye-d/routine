package org.fishnpotatoes.routine

import org.fishnpotatoes.routine.RoutineManager.restartableRoutines


private typealias Action = () -> Unit

/**
 * The thing that runs the [Routine]s
 */
object RoutineManager {
    const val INDENT = "  "

    internal val routines = mutableListOf<RoutineBuilder>()
    internal val restartableRoutines = mutableListOf<RoutineBuilder>()
    internal val activeLocks = mutableMapOf<Any, RoutineBuilder>()
    internal val deferredActions = ArrayDeque<Action>(0)
    internal val subsystems = mutableListOf<Subsystem>()
    private val triggers = ArrayList<TriggeredAction>()

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
                    unlock(routine)
                }
            }
        }
        subsystems.forEach(Subsystem::tick)
        runDeferred()
        for ((trigger, _, action) in triggers) {
            if (trigger()) action()
        }
        runDeferred()
        for (routine in restartableRoutines) {
            val conflicts = conflictingRoutines(routine.locks)
            if (conflicts.isEmpty()) {
                defer {
                    routine.run(false)
                    restartableRoutines.remove(routine)
                }
            }
        }
        while (deferredActions.isNotEmpty()) runDeferred()
        return hasCommands
    }

    fun registerSubsystem(subsystem: Subsystem) {
        subsystems.add(subsystem)
    }

    data class TriggeredAction(
        val trigger: () -> Boolean,
        val originalTrigger: (() -> Boolean)? = null,
        val action: () -> Unit,
    )

    fun bind(trigger: () -> Boolean, originalTrigger: (() -> Boolean)?, action: () -> Unit) {
        val ta = TriggeredAction(trigger, originalTrigger, action)
        triggers.add(ta)
    }

    /**
     * Run [action] on the rising edge of the boolean supplier.
     *
     * @param action The action to run
     * @return The same boolean supplier for chaining calls
     */
    fun <T : () -> Boolean> T.onceOnTrue(action: () -> Unit): T {
        var lastVal = this()
        bind({
            val thisVal = this()
            val ret = thisVal && !lastVal
            lastVal = thisVal
            ret
        }, this, action)
        return this
    }

    /**
     * Run [action] on the falling edge of the boolean supplier.
     *
     * @param action The action to run
     * @return The same boolean supplier for chaining calls
     */
    fun <T : () -> Boolean> T.onceOnFalse(action: () -> Unit): T {
        var lastVal = this()
        bind({
            val thisVal = this()
            val ret = !thisVal && lastVal
            lastVal = thisVal
            ret
        }, this, action)
        return this
    }

    /**
     * Run [risingAction] on the rising edge of the boolean supplier and [fallingAction] on the falling edge.
     *
     * @param risingAction The action to run on the rising edge
     * @param fallingAction The action to run on the falling edge
     * @return The same boolean supplier for chaining calls
     */
    fun <T : () -> Boolean> T.whileOnTrue(risingAction: () -> Unit, fallingAction: () -> Unit) =
        onceOnTrue(risingAction).onceOnFalse(fallingAction)

    private fun runDeferred() {
        while (deferredActions.isNotEmpty()) {
            deferredActions.removeFirst()()
        }
    }

    internal fun defer(block: Action) = deferredActions.add(block)
    internal fun lock(routine: RoutineBuilder) {
        activeLocks.putAll(routine.locks.map { it to routine })
    }

    internal fun unlock(routine: RoutineBuilder) {
        routine.locks.forEach(activeLocks::remove)
    }

    internal fun conflictingRoutines(locks: Set<Any>) =
        locks.filter(activeLocks::contains).map { activeLocks[it]!! }.toSet()

    override fun toString() = buildString {
        if (routines.isNotEmpty()) {
            appendLine("Routines:")
            appendLine(routines.joinToString("\n") {
                buildString {
                    append(it)
                    if (it.locks.isEmpty()) {
                        append(" \uD83D\uDD12 ")
                        append(it.locks.joinToString(", ") { lock -> lock.toString() })
                    }
                }
            }.prependIndent(INDENT))
        }
        if (restartableRoutines.isNotEmpty()) {
            appendLine("Restarting routines:")
            appendLine(restartableRoutines.joinToString("\n") {
                "$it${if (it.locks.isEmpty()) "" else " \uD83D\uDD12 "}${it.locks.joinToString(", ")}"
            }.prependIndent(INDENT))
        }
        if (subsystems.isNotEmpty()) {
            appendLine("Subsystems:")
            appendLine(subsystems.joinToString("\n") {
                buildString {
                    append(it.toString())
                    if (it in activeLocks) {
                        append(" \uD83D\uDD12")
                    }
                }
            }.prependIndent(INDENT))
        }
    }
}

fun RoutineBuilder.run(interruptOther: Boolean = true) = RoutineManager.defer {
    val conflicts = RoutineManager.conflictingRoutines(this.locks)
    if (!interruptOther && !conflicts.isEmpty()) {
        return@defer
    }

    for (conflict in conflicts) {
        if (conflict.restart) {
            restartableRoutines.add(conflict)
            RoutineManager.routines.remove(conflict)
            RoutineManager.unlock(conflict)
        } else {
            conflict.interrupt()
        }
    }

    RoutineManager.routines.add(this)
    RoutineManager.lock(this)
}

fun RoutineBuilder.interrupt() {
    interruptRoutine()
    RoutineManager.routines.remove(this)
    RoutineManager.unlock(this)
}
