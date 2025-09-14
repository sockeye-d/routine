package org.fishnpotatoes.routine

abstract class Subsystem(val name: String? = null) {
    init {
        RoutineManager.registerSubsystem(this)
    }
    abstract fun tick()

    override fun toString() = name ?: this::class.simpleName ?: "<anonymous>"
}
