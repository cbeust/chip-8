package dev.johnoreilly.chip_8_kmm.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.beust.chip8.Display
import dev.johnoreilly.chip8.Emulator


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val romFile = resources.assets.open("Space Invaders [David Winter].ch8")
        val romData = romFile.readBytes()
        setContent {
            MaterialTheme {
                MainLayout(romData)
            }
        }
    }
}

@Composable
fun MainLayout(romData: ByteArray) {
    val emulator = remember {
        Emulator().also {
            it.loadRom(romData)
        }
    }

    Column(modifier = Modifier
        .fillMaxHeight()
        .padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
        EmulatorView(emulator)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            GameButton(emulator, 4, Icons.Filled.ArrowBack)
            GameButton(emulator, 5, Icons.Filled.ArrowUpward)
            GameButton(emulator, 6, Icons.Filled.ArrowForward)
        }
    }
}

@Composable
fun GameButton(emulator: Emulator, number: Int, icon: ImageVector) {
    Button(modifier = Modifier.pointerInteropFilter {
        when (it.action) {
            MotionEvent.ACTION_DOWN -> {
                emulator.keyPressed(number)
            }
            MotionEvent.ACTION_UP -> {
                emulator.keyReleased()
            }
            else ->  false
        }
        true
    }, onClick = {}) {
        Icon(imageVector = icon, contentDescription = "$number")
    }
}

@Composable
fun EmulatorView(emulator: Emulator) {
    val screenData = produceState<IntArray?>(null, emulator) {
        emulator.observeScreenUpdates {
            value = it
        }
    }

    val displayWidth = Display.WIDTH
    val displayHeight = Display.HEIGHT

    screenData.value?.let { screenData ->
        BoxWithConstraints {
            val blockSize = constraints.maxWidth / displayWidth

            Canvas(modifier = Modifier.fillMaxWidth()) {
                repeat(displayWidth) { x ->
                    repeat(displayHeight) { y ->
                        val index = x + displayWidth * y
                        if (screenData[index] == 1) {
                            val xx = blockSize * x.toFloat()
                            val yy = blockSize * y.toFloat()

                            drawRect(Color.Black, topLeft = Offset(xx, yy),
                                size = Size(blockSize.toFloat(), blockSize.toFloat())
                            )
                        }
                    }
                }
            }
        }
    }

}


