package dev.fishies.routine


private typealias Action = () -> Unit

/**
 * The thing that runs the [RoutineScope]s
 */
object RoutineManager {
    const val INDENT = "  "

    private val routines = mutableListOf<Routine>()
    private val restartableRoutines = mutableListOf<Routine>()
    private val activeLocks = mutableMapOf<Any, Routine>()
    private val deferredActions = ArrayDeque<Action>(0)
    private val subsystems = mutableSetOf<Subsystem>()
    private val triggers = ArrayList<TriggeredAction>()

    /**
     * Whether the manager still has active routines.
     */
    val hasRoutines
        get() = routines.isNotEmpty()

    /**
     * Reset the internal state of the [RoutineManager].
     * It's recommended to call this at the end of your opmodes as part of the deinitialization process.
     */
    fun reset() {
        routines.clear()
        restartableRoutines.clear()
        activeLocks.clear()
        deferredActions.clear()
        subsystems.clear()
        triggers.clear()

        Routine.State.i = 0
    }

    /**
     * Run a single tick of on all scheduled routines, check and activate triggers, and restart eligible restartable
     * routines.
     *
     * @return Whether the manager still has active routines
     */
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
                    routine.start(false)
                    restartableRoutines.remove(routine)
                }
            }
        }
        while (deferredActions.isNotEmpty()) runDeferred()
        return hasRoutines
    }

    /**
     * Register a [Subsystem] so that its [Subsystem.tick] gets called every [tick].
     *
     * You don't really need to call this method manually,
     * since [Subsystem]-derived classes automatically register themselves on creation.
     */
    fun registerSubsystem(subsystem: Subsystem) {
        subsystems.add(subsystem)
    }

    private fun bind(trigger: () -> Boolean, originalTrigger: (() -> Boolean)?, action: () -> Unit) {
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

    internal fun defer(block: Action) {
        deferredActions.add(block)
    }

    internal fun lock(routine: Routine) {
        activeLocks.putAll(routine.locks.map { it to routine })
    }

    internal fun unlock(routine: Routine) {
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

    /**
     * Run this routine.
     * If this routine's [RoutineScope.locks] conflicts with any currently running routines'
     * [RoutineScope.locks], then one of two things will happen based on the value of [interruptOther]:
     * * true → currently running routine gets interrupted
     * * false → this routine doesn't get scheduled
     */
    fun Routine.start(interruptOther: Boolean = true) = defer {
        val conflicts = conflictingRoutines(this.locks)
        if (!interruptOther && !conflicts.isEmpty()) {
            return@defer
        }

        for (conflict in conflicts) {
            if (conflict.restart) {
                restartableRoutines.add(conflict)
                routines.remove(conflict)
                unlock(conflict)
            } else {
                conflict.interrupt()
            }
        }

        routines.add(this)
        lock(this)
    }

    /**
     * Interrupt a routine.
     *
     * I don't know why you would want to do this, since this is mostly for internal use, but you certainly can.
     */
    fun Routine.interrupt() {
        interruptRoutine()
        routines.remove(this)
        unlock(this)
    }
}

private data class TriggeredAction(
    val trigger: () -> Boolean,
    val originalTrigger: (() -> Boolean)? = null,
    val action: () -> Unit,
)