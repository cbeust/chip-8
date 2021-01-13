package dev.johnoreilly.chip8

import com.beust.chip8.Computer
import com.beust.chip8.Display

class Emulator {
    private val display = ComposeDisplay()
    private val computer = Computer(display)

    fun loadRom(romData: ByteArray) {
        computer.stop()
        computer.loadRom(romData)
    }

    fun observeScreenUpdates(success: (IntArray) -> Unit) {
        display.setScreenCallback {
            success(it)
        }
    }

    fun keyPressed(key: Int) {
        computer.keyboard.key = key
    }

    fun keyReleased() {
        computer.keyboard.key = null
    }
}


class ComposeDisplay() : Display {
    private var screenCallback: ((IntArray) -> Unit)? = null

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
