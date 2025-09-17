package dev.fishies.routine

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.createCoroutineUnintercepted

/**
 * Represents a single action.
 */
interface RoutineScope {
    /**
     * Whether to restart this command after being interrupted by another command.
     * Restarting commands get shuffled off to a queue to get retried each tick if they don't conflict with any other
     * commands.
     *
     * A restarting command is effectively equivalent to a default command, but it's not tied to a specific subsystem
     * (because routine doesn't really have those).
     *
     * Developer notes: I was considering making all commands that don't break after being interrupted restarting
     * commands, but I have since deemed that "too implicit"
     */
    var restart: Boolean

    /**
     * The locks a routine holds.
     * Two routines that hold conflicting locks won't run at the same time.
     *
     * You can lock any object you want, but generally a [Subsystem] instance is a good idea.
     */
    val locks: Set<Any>

    /**
     * Whether the routine is finished or not.
     *
     * Developer note: This is generally a read-only view into a mutable backing field.
     */
    val finished: Boolean

    /**
     * The name of the routine.
     */
    val name: String

    /**
     * The type of the routine, e.g. a [dev.fishies.routine.compose.parallel] would be `"parallel"`
     */
    val typeName: String
    var display: () -> String

    /**
     * Yield until the next tick.
     *
     * @return True if the command was interrupted and needs to exit.
     * Commonly used like
     * ```kt
     * if (yield()) break
     * ```
     */
    suspend fun yield(): Boolean

    /**
     * Indicate readiness after initial setup.
     */
    suspend fun ready()

    /**
     * Adds a set of objects to the list of locks in the routine.
     * If you're adding multiple locks from a list,
     * prefer [requiresAll].
     */
    fun requires(vararg any: Any)

    /**
     * Adds `this` the list of locks in the routine.
     */
    fun Any.lock() = requires(this)

    /**
     * Run another routine as a subroutine of this routine.
     *
     * **NOTE**: Child routines **do not** propagate their locks to their parents!
     */
    suspend fun await(subroutine: Routine) {
        while (!subroutine.finished) {
            subroutine.runSingleStep()
            yield()
        }
    }
}

/**
 * Adds a collection of objects to the list of locks in the routine.
 */
fun RoutineScope.requiresAll(any: Collection<Any>) = requires(*any.toTypedArray())

class Routine internal constructor(
    override val name: String,
    override val typeName: String,
) : RoutineScope {
    internal lateinit var startContinuation: Continuation<Unit>
    internal lateinit var yieldContinuation: Continuation<Boolean>

    private val initialized get() = ::yieldContinuation.isInitialized

    private val mutRequirements = mutableSetOf<Any>()
    override val locks: Set<Any>
        get() = mutRequirements
    override var restart: Boolean = false
    internal var _finished: Boolean = false
    override val finished get() = _finished
    override var display = { name }
    internal var _interrupted = false
    val interrupted get() = _interrupted

    private var tick = 0

    fun runInit() {
        startContinuation.resume(Unit)
        assert(!finished) { "$this finished before ticking (did you forget ready()?)" }
    }

    fun runSingleStep() {
        assert(!finished) { "$this Already finished (did you attempt to reuse a command?)" }
        if (initialized) {
            yieldContinuation.resume(false)
        } else {
            startContinuation.resume(Unit)
        }
        tick++
    }

    fun interruptRoutine() {
        if (initialized && !finished) {
            yieldContinuation.resumeWithException(RoutineInterruptedException())
        }
        _interrupted = true
        _finished = true
    }

    override suspend fun yield(): Boolean =
        if (finished) error("Attempting to yield after being interrupted (did you forget to break?)") else suspendCoroutine {
            yieldContinuation = it
        }


    override suspend fun ready(): Unit = suspendCoroutine {
        startContinuation = it
    }

    override fun requires(vararg any: Any) {
        mutRequirements.addAll(any)
    }

    internal object State {
        var i: Int = 0
            get() = field++
    }

    private val idle = charArrayOf(
        '⡇',
        '⠏',
        '⠛',
        '⠹',
        '⢸',
        '⣰',
        '⣤',
        '⣆',
    )

    private fun idleDisplay() = if (finished || tick <= 1) " " else idle[(tick / 2) % idle.size]
    override fun toString() = "${idleDisplay()} ($typeName) ${display()}"
}

/**
 * Thrown when a routine is interrupted, normally by another routine with conflicting locks being scheduled.
 * Catch it in a try/finally block to clean up after interruption.
 */
class RoutineInterruptedException : Throwable()

/**
 * Creates a routine from a given block of code.
 *
 * A basic routine represents a single action to be executed.
 * Multiple routines can be run at the same time with the cooperatively multitasking coroutine model,
 * where each routine "takes turns" with the other routines by yielding control flow with [RoutineScope.yield].
 *
 * Individual routines can be composed together with groups like [dev.fishies.routine.compose.serial] to perform more
 * complex actions, or decorated with functions like [dev.fishies.routine.compose.timeout] to add special effects to
 * each routine.
 *
 * Routines can also lock objects with [RoutineScope.lock] or [RoutineScope.requires] like a `synchronized` block would, so that
 * two routines that lock the same object can't run at the same time.
 *
 * Running routines are managed by the (surprise) [RoutineManager].
 *
 * Finally, unlike in other command implementations, routines cannot be restarted.
 * Instead, make a new one.
 */
fun routine(
    name: String? = null,
    typeName: String = "anonymous",
    onInterrupted: () -> Unit = {},
    block: suspend RoutineScope.() -> Unit,
): Routine {
    val builder = Routine(name ?: "Routine${Routine.State.i}", typeName)
    builder.startContinuation = block.createCoroutineUnintercepted(builder, Continuation(EmptyCoroutineContext) {
        if (it.isFailure) {
            if (it.exceptionOrNull() is RoutineInterruptedException) {
                onInterrupted()
            } else {
                throw it.exceptionOrNull()!!
            }
        }
        builder._finished = true
    })
    builder.runInit()
    return builder
}

/**
 * Run [block] forever, yielding after each iteration.
 */
suspend inline fun RoutineScope.forever(block: suspend RoutineScope.() -> Unit) {
    while (true) {
        this.block()
        yield()
    }
}

/**
 * Run [block] while [condition] is true, yielding after each iteration.
 */
suspend inline fun RoutineScope.yieldWhile(
    condition: RoutineScope.() -> Boolean,
    block: suspend RoutineScope.() -> Unit,
) {
    while (condition()) {
        this.block()
        yield()
    }
}