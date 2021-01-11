import androidx.compose.desktop.Window
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusReference
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusReference
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.unit.dp
import com.beust.chip8.Computer
import com.beust.chip8.Display
import javafx.scene.layout.Pane
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.File
import java.nio.file.Paths



data class ScreenState(var pixels: IntArray? = null)

fun main() = Window {
    var currentRomPath = ""
    var currentRomName = ""
    val focusRequester = remember { FocusReference() }

    val composeDisplay = remember { ComposeDisplay() }
    val computer = remember {
        Computer(display = composeDisplay).apply {
            val spaceInvaders = Paths.get("roms", "Space Invaders [David Winter].ch8")
            currentRomPath = spaceInvaders.toFile().absolutePath

            // Restart computer
            stop()
            loadRom(File(currentRomPath))

            // Update the rom name
            val start = currentRomPath.lastIndexOf(File.separatorChar)
            val end = currentRomPath.lastIndexOf(".ch8")
            val romName = if (end == -1) currentRomPath.substring(start + 1)
            else currentRomPath.substring(start + 1, end)
            currentRomName = romName
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.shortcuts {
                on(Key.Number4) {
                    computer.keyboard.key = 4
                }
                on(Key.Number5) {
                    computer.keyboard.key = 5
                }
                on(Key.Number6) {
                    computer.keyboard.key = 6
                }
            }
            .focusReference(focusRequester)
            .focusModifier()
            .clickable(indication = null) { focusRequester.requestFocus() }
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Text(currentRomName, style = MaterialTheme.typography.h3)
                }
                EmulatorView(computer, composeDisplay)
            }
        }
    }


}

@Composable
fun EmulatorView(computer: Computer, display: ComposeDisplay) {
    var blockWidth = 24
    var blockHeight = 24
    val SPACE = 0

    val scope = MainScope()
    val screenState = produceState(ScreenState(), display) {
        getScreenUpdates(display)
            .onEach { value = it }
            .catch { println("catch" )}
            .onCompletion { println("onCompletion") }
            .launchIn(scope)
    }

    screenState.value.pixels?.let { pixels ->
        Canvas(modifier = Modifier.fillMaxSize().background(Color.White)) {
            repeat(Display.WIDTH) { x ->
                repeat(Display.HEIGHT) { y ->
                    val index = x + Display.WIDTH * y
                    if (pixels[index] == 1) {
                        val xx = (blockWidth + SPACE) * x.toFloat()
                        val yy = (blockHeight + SPACE) * y.toFloat()

                        drawRect(Color.Black, topLeft = Offset(xx, yy), size = Size(blockHeight.toFloat(), blockHeight.toFloat()))
                    }
                }
            }
        }
    }

}

fun getScreenUpdates(display: ComposeDisplay) = callbackFlow {
    display.setScreenCallback {
        offer(ScreenState(it))
    }

    awaitClose{
        println("close")
    }
}


class ComposeDisplay() : Display {
    private var screenCallback: ((IntArray) -> Unit)? = null

    override val pane: Pane
        get() = TODO("Not yet implemented")

    override fun draw(frameBuffer: IntArray) {
        val screen = IntArray(Display.WIDTH * Display.HEIGHT)
        frameBuffer.copyInto(screen, 0)
        screenCallback?.invoke(screen)
    }

    override fun clear(frameBuffer: IntArray) {
    }

    fun setScreenCallback(screenCallback: (IntArray) -> Unit) {
        this.screenCallback = screenCallback
    }
}



