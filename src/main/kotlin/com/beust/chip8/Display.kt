@file:Suppress("unused")

package com.beust.chip8

import javafx.beans.Observable
import javafx.scene.canvas.Canvas
import javafx.scene.control.TextArea
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

/**
 * Interface for rendering on a display.
 */
interface Display {
    val pane: Pane
    fun draw(frameBuffer: IntArray)
    fun clear(frameBuffer: IntArray)

    companion object {
        const val WIDTH: Int = 64
        const val HEIGHT: Int = 32
    }

    fun index(x: Int, y: Int) = x + WIDTH * y
}

/**
 * Display the frame buffer on a TextArea.
 */
class DisplayText: Display, Pane() {
    override val pane = this

    override fun draw(frameBuffer: IntArray) {
        val t = StringBuffer()
        repeat(32) { y ->
            repeat(64) { x ->
                t.append(if (frameBuffer[index(x, y)] == 0) " " else "*")
            }
            t.append("\n")
        }
        textArea.text = t.toString()
    }

    override fun clear(frameBuffer: IntArray) {
        textArea.clear()
    }

    private val textArea = TextArea()

    init {
        prefWidth = 800.0
        prefHeight = 600.0
        textArea.prefWidth = prefWidth
        textArea.prefHeight = prefHeight
        VBox.setVgrow(textArea, Priority.ALWAYS)
        children.add(textArea)
    }
}

/**
 * Display the frame buffer on a canvas.
 */
class DisplayGraphics : Display, Pane() {
    private var blockWidth = 12
    private var blockHeight = 12

    private val SPACE = 0
    private val SCREEN = IntArray(Display.WIDTH * Display.HEIGHT)
    private val canvas = Canvas((Display.WIDTH + SPACE) * blockWidth.toDouble(),
            (Display.HEIGHT + SPACE) * blockHeight.toDouble())

    init {
        widthProperty().addListener { e: Observable? ->
            canvas.width = width
            blockWidth = (canvas.width / (Display.WIDTH + SPACE)).toInt()
        }
        heightProperty().addListener { e: Observable? ->
            canvas.height = height
            blockHeight = (canvas.height / (Display.HEIGHT + SPACE)).toInt()
        }
        children.add(canvas)
        VBox.setVgrow(this, Priority.ALWAYS)
    }

    override val pane = this

    override fun clear(frameBuffer: IntArray) {
        frameBuffer.fill(0)
    }

    override fun draw(frameBuffer: IntArray) {
        frameBuffer.copyInto(SCREEN, 0)
        requestLayout()
    }

    @Override
    override fun layoutChildren() {
        // Clear the whole canvas
        val g = canvas.graphicsContext2D
        g.fill = Color.WHITE
        g.fillRect(0.0, 0.0, (blockWidth + SPACE) * Display.WIDTH.toDouble(),
                (blockHeight + SPACE) * Display.HEIGHT.toDouble())

        // Only draw the black blocks
        g.fill = Color.BLACK
        repeat(Display.WIDTH) { x ->
            repeat(Display.HEIGHT) { y ->
                if (SCREEN[index(x, y)] == 1) {
                    val xx = (blockWidth + SPACE) * x.toDouble()
                    val yy = (blockHeight + SPACE) * y.toDouble()
                    g.fillRect(xx, yy, blockWidth.toDouble(), blockHeight.toDouble())
                }
            }
        }
    }

}
