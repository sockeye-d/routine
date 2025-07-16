import org.fishnpotatoes.routine.RoutineManager
import org.fishnpotatoes.routine.groups.parallel
import org.fishnpotatoes.routine.routine
import org.fishnpotatoes.routine.run
import java.lang.Thread.sleep

fun basicRoutine(message: String, lock: Any? = null) = routine {
    lock?.lock()
    ready()
    for (i in 1..10) {
        println("$message $i")
        if (yield()) break
    }
    println("deinit $message")
}

fun main() {
    val lock = object {}

    parallel(
        basicRoutine("hi", lock),
        basicRoutine("hello"),
    ).run()

    while (RoutineManager.tick()) {
        sleep(100)
    }
}
