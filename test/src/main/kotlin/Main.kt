import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import org.fishnpotatoes.routine.RoutineManager
import org.fishnpotatoes.routine.groups.wait
import org.fishnpotatoes.routine.run
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun App() {
    var schedulerState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(33.milliseconds)
            RoutineManager.tick()
            schedulerState = RoutineManager.toString()
        }
    }

    Column {
        Button({
            wait(1.seconds).run()
        }) {
            Text("run thingy")
        }
        Text(schedulerState, fontFamily = FontFamily.Monospace)
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}