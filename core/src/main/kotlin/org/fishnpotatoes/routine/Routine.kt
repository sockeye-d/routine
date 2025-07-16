package org.fishnpotatoes.routine

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.intrinsics.*
import kotlin.coroutines.resume

interface Routine {
    suspend fun yield(): Boolean
    suspend fun init()

    fun requires(any: Any)
}

class RoutineBuilder internal constructor() : Routine {
    internal lateinit var startContinuation: Continuation<Unit>
    internal lateinit var yieldContinuation: Continuation<Boolean>
    var finished = false

    private val mutRequirements = mutableSetOf<Any>()
    val requirements
        // please don't downcast this
        get() = mutRequirements

    internal fun runSingleStep() {
        if (finished) {
            throw IllegalStateException("Already finished. Generate a new command, please.")
        }
        if (::yieldContinuation.isInitialized) {
            yieldContinuation.resume(false)
        } else {
            startContinuation.resume(Unit)
        }
    }

    internal fun interruptRoutine() {
        yieldContinuation.resume(true)
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

    override suspend fun init() {
        return suspendCoroutine {
            startContinuation = it
        }
    }

    override fun requires(any: Any) {
        mutRequirements.add(any)
    }
}

fun routine(block: suspend Routine.() -> Unit): RoutineBuilder {
    val builder = RoutineBuilder()
    builder.startContinuation = block.createCoroutineUnintercepted(builder, Continuation(EmptyCoroutineContext) {
        if (it.isFailure) {
            throw it.exceptionOrNull()!!
        }
        builder.finished = true
    })
    builder.runSingleStep()
    return builder
}