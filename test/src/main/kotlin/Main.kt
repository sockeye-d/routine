import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import org.fishnpotatoes.routine.RoutineManager
import org.fishnpotatoes.routine.groups.parallel
import org.fishnpotatoes.routine.groups.serial
import org.fishnpotatoes.routine.groups.wait
import org.fishnpotatoes.routine.routine
import org.fishnpotatoes.routine.run
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTextApi::class)
@Composable
fun App() {
    var schedulerState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(33.milliseconds)
            RoutineManager.tick()
            schedulerState = "$RoutineManager"
        }
    }

    Column {
        Button({
            parallel(
                wait(1.seconds),
                wait(2.seconds),
                serial(
                    wait(1.seconds),
                    wait(3.seconds),
                ),
            ).run()

            routine {
                ready()
                for (i in 1..10) {
                    println(i)
                    if (yield()) break
                }
            }.run()
        }) {
            Text("run thingy")
        }
        Text(schedulerState, fontFamily = FontFamily("Iosevka"))
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}