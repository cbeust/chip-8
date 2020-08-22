package com.beust.chip8

@Suppress("PrivatePropertyName")
class FrameBuffer {
    companion object {
        const val WIDTH = 64
        const val HEIGHT = 32
    }

    val frameBuffer = IntArray(WIDTH * HEIGHT)

    fun pixel(x: Int, y: Int): Int = frameBuffer[y * WIDTH + x]

    fun setPixel(x: Int, y: Int, v: Int) {
        frameBuffer[y * WIDTH + x] = v
    }

    fun clear() {
        frameBuffer.fill(0)
    }
    /**
     * Display the frame buffer in the console (debug).
     */
    @Suppress("unused")
    fun show() {
        repeat(HEIGHT) { y ->
            repeat(WIDTH) { x ->
                val c = if (pixel(x, y) == 0 ) "." else "X"
                print(c)
            }
            println("")
        }
    }
}