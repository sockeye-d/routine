import org.fishnpotatoes.routine.RoutineManager
import org.fishnpotatoes.routine.routine
import org.fishnpotatoes.routine.run

fun main() {
    val lock = object {}

    val rt1 = routine {
        requires(lock)
        init()
        for (i in 1..10) {
            println("hello $i")
            if (yield()) {
                println("returning")
                return@routine
            }
        }
    }
    val rt2 = routine {
        requires(lock)
        init()
        for (i in 1..10) {
            println("hi $i")
            if (yield())
                return@routine
        }
    }

    rt1.run()

    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    rt2.run()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
    RoutineManager.tick()
}
