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
inline fun Routine.requiresAll(any: Collection<Any>) = requires(*any.toTypedArray())

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
    override var display = { name }
    internal var hasRun = false

    fun runSingleStep() {
        if (finished) throw IllegalStateException("Already finished. Generate a new command, please.")
        if (initialized) {
            yieldContinuation.resume(false)
        } else {
            startContinuation.resume(Unit)
        }
    }

    fun interruptRoutine() {
        if (initialized) yieldContinuation.resume(true)
        finished = true
    }

    override suspend fun yield(): Boolean {
        if (finished) {
            error("Attempting to yield after being interrupted.")
        }

        return suspendCoroutine {
            yieldContinuation = it
        }
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

    override fun toString() = display()
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
    builder.runSingleStep()
    return builder
}

fun Routine.setup(block: Routine.() -> Unit) = this.block()