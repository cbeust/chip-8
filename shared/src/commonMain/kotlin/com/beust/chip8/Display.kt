@file:Suppress("unused")

package com.beust.chip8



/**
 * Interface for rendering on a display.
 */
interface Display {
    fun draw(frameBuffer: IntArray)
    fun clear(frameBuffer: IntArray)

    companion object {
        const val WIDTH: Int = 64
        const val HEIGHT: Int = 32
    }

    fun index(x: Int, y: Int) = x + WIDTH * y
}

