import jdk.internal.joptsimple.internal.Messages.message
import org.fishnpotatoes.routine.RoutineManager
import org.fishnpotatoes.routine.groups.parallel
import org.fishnpotatoes.routine.groups.sequential
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
    val drivetrain = DrivetrainSubsystem()

    sequential(
        basicRoutine("hi"),
        basicRoutine("hello"),
    ).run()

    //val rt = routine {
    //    lock.lock()
    //    ready()
    //    for (i in 1..10) {
    //        println("hi $i")
    //        if (yield()) break
    //    }
    //    println("deinit hi")
    //}

    //rt.run()

    val drive = routine {
        drivetrain.lock()
        ready()
        while (true) {
            drivetrain.drive(1.0, 0.0, 0.0)
            if (yield()) break
        }
    }

    while (RoutineManager.tick()) {
        sleep(100)
    }
}

private class DrivetrainSubsystem() {
    fun drive(x: Double, y: Double, heading: Double) {}
}