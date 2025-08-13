package org.fishnpotatoes.routine

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.createCoroutineUnintercepted
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Represents a single action.
 */
interface Routine {
    val locks: Set<Any>
    var finished: Boolean
    var name: String
    var typeName: String
    var display: () -> String
    suspend fun yield(): Boolean
    suspend fun ready()

    /**
     * Adds a set of objects to the list of locks in the routine.
     * If you're adding multiple locks from a list,
     * prefer [requiresAll].
     */
    fun requires(vararg any: Any)
    fun Any.lock() = requires(this)
}

/**
 * Adds a collection of objects the list of locks in the routine.
 */
fun Routine.requiresAll(any: Collection<Any>) = requires(*any.toTypedArray())

class RoutineBuilder internal constructor() : Routine {
    internal lateinit var startContinuation: Continuation<Unit>
    internal lateinit var yieldContinuation: Continuation<Boolean>

    private val initialized get() = ::yieldContinuation.isInitialized

    private val mutRequirements = mutableSetOf<Any>()
    override val locks
        // please don't downcast this
        get() = mutRequirements
    override var finished: Boolean = false
    override var name: String = "Routine${State.i}"
    override var typeName: String = ""
    override var display = { name }
    internal var hasRun = false

    private var tick = 0

    fun runInit() {
        startContinuation.resume(Unit)
        assert(!finished) { "Command finished before ticking (did you forget ready()?)" }
    }

    fun runSingleStep() {
        assert(!finished) { "Already finished (did you attempt to reuse a command?)" }
        if (::yieldContinuation.isInitialized) {
            yieldContinuation.resume(false)
        } else {
            startContinuation.resume(Unit)
        }
        tick++
    }

    fun interruptRoutine() {
        if (::yieldContinuation.isInitialized) yieldContinuation.resume(true)
        finished = true
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

    private object State {
        private var _i = 0
        val i
            get() = _i++
    }

    val idle = charArrayOf(
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
 * Creates a routine from a given block of code.
 * Use [Routine.ready] and [Routine.yield] to control the execution flow,
 * and [Routine.lock] to control the requirements.
 */
fun routine(block: suspend Routine.() -> Unit): RoutineBuilder {
    val builder = RoutineBuilder()
    builder.startContinuation = block.createCoroutineUnintercepted(builder, Continuation(EmptyCoroutineContext) {
        if (it.isFailure) {
            throw it.exceptionOrNull()!!
        }
        builder.finished = true
    })
    // run init step
    builder.runInit()
    return builder
}

fun Routine.setup(block: Routine.() -> Unit) = this.block()