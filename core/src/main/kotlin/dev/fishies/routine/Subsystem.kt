package dev.fishies.routine

/**
 * The base class of all subsystems.
 * A subsystem gets its [tick] function called every tick by the [RoutineManager].
 */
abstract class Subsystem(val name: String? = null) {
    init {
        RoutineManager.registerSubsystem(this)
    }

    open fun tick() {}

    override fun toString() = name ?: this::class.simpleName ?: "<anonymous>"
}
