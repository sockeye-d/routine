import org.fishnpotatoes.routine.RoutineManager
import org.fishnpotatoes.routine.routine
import org.fishnpotatoes.routine.run

fun customCommand(subsystem: Any?, label: String) = routine {
    subsystem?.lock()
    ready()
    for (i in 1..10) {
        println("$label: $i")
        if (yield()) break
    }
    println("$label done")
}

fun main() {
    val subsystem = object {}

    val rt1 = customCommand(subsystem, "a")

    val rt2 = customCommand(subsystem, "b")

    rt1.run()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    rt2.run(interruptOther = false)

    @Suppress("ControlFlowWithEmptyBody")
    while (RoutineManager.tick()) {
    }
}