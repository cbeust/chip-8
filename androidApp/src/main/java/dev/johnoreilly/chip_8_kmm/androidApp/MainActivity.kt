package dev.johnoreilly.chip_8_kmm.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.platform.setContent
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

    Column {
        EmulatorView(emulator)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            GameButton(emulator, 4)
            GameButton(emulator, 5)
            GameButton(emulator, 6)
        }
    }
}

@Composable
fun GameButton(emulator: Emulator, number: Int) {
    Button(onClick = { emulator.keyPressed(number) }) {
        Text(number.toString())
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

            Canvas(modifier = Modifier.fillMaxWidth().preferredHeight(200.dp).background(Color.White)) {

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


