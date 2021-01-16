import androidx.compose.desktop.Window
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.dp
import com.beust.chip8.Computer
import com.beust.chip8.Display
import dev.johnoreilly.chip8.Emulator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.readBytes


fun main() = Window {
    val romFile = Paths.get("roms", "Space Invaders [David Winter].ch8")
    val currentRomPath = romFile.toFile().absolutePath
    val romData = File(currentRomPath).readBytes()

    // Update the rom name
    val start = currentRomPath.lastIndexOf(File.separatorChar)
    val end = currentRomPath.lastIndexOf(".ch8")
    val romName = if (end == -1) currentRomPath.substring(start + 1)
    else currentRomPath.substring(start + 1, end)


    MaterialTheme {
        MainLayout(romData, romName)
    }
}

@Composable
fun MainLayout(romData: ByteArray, romName: String) {
    val focusRequester = remember { FocusReference() }

    val emulator = remember {
        Emulator().also {
            it.loadRom(romData)
        }
    }

    Column(modifier = Modifier
        .onKeyEvent {
            if (it.type == KeyEventType.KeyDown) {
                emulator.keyPressed(it.key.keyCode - 48)
            } else if (it.type == KeyEventType.KeyUp) {
                emulator.keyReleased()
            }
            true
        }
        .focusReference(focusRequester)
        .focusModifier()
        .clickable(indication = null) { focusRequester.requestFocus() }
        .padding(16.dp))
    {

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(romName, style = MaterialTheme.typography.h3)
        }

        EmulatorView(emulator)
    }
}

@Composable
fun EmulatorView(emulator: Emulator) {
    val screenData = produceState<IntArray?>(null, emulator) {
        emulator.observeScreenUpdates {
            value = it
        }
    }

    screenData.value?.let { screenData ->
        WithConstraints {
            val blockWidth = constraints.maxWidth / Display.WIDTH
            val blockHeight = blockWidth

            Canvas(modifier = Modifier.fillMaxSize().background(Color.White)) {

                repeat(Display.WIDTH) { x ->
                    repeat(Display.HEIGHT) { y ->
                        val index = x + Display.WIDTH * y
                        if (screenData[index] == 1) {
                            val xx = blockWidth * x.toFloat()
                            val yy = blockHeight * y.toFloat()

                            drawRect(
                                Color.Black,
                                topLeft = Offset(xx, yy),
                                size = Size(blockHeight.toFloat(), blockHeight.toFloat())
                            )
                        }
                    }
                }
            }
        }
    }

}


