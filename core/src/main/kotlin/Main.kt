import dev.fishies.routine.RoutineManager
import dev.fishies.routine.RoutineManager.start
import dev.fishies.routine.routine

fun printStuffRoutine(subsystem: Any?, label: String, iterations: Int = 10) = routine(name = label) {
    subsystem?.lock()
    ready()
    try {
        for (i in 1..iterations) {
            println("$label: $i")
            yield()
        }
    } finally {
        println("$label done")
    }
}

fun main() {
    val subsystem = object {}

    val rt3 = routine(name = "rt3") {
        subsystem.lock()
        ready()
        try {
            await(routine {
                ready()
                await(printStuffRoutine(null, "a"))
            })
            await(printStuffRoutine(subsystem, "b"))
            await(printStuffRoutine(subsystem, "c"))
        } finally {
            println("hi")
        }
    }

    rt3.start()

    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()

    printStuffRoutine(subsystem, "d", iterations = 11).start()

    while (RoutineManager.tick());
}