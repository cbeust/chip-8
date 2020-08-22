package com.beust.chip8

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class OpTest {
    private fun readPixels(c: Computer, w: Int, h: Int): List<Int> {
        val result = arrayListOf<Int>()
        repeat(w) { y ->
            repeat(h) { x ->
                result.add(c.frameBuffer.pixel(x, y))
            }
        }
        return result
    }

    fun draw() {
        val cpu = Cpu().apply {
            I = 0x200
        }
        val c = Computer(cpu = cpu, sound = false)
        val bytes = listOf(0xf0, 0x90, 0xf0, 0x90, 0xf0)
        val memory = c.cpu.memory
        bytes.forEachIndexed {
            index, b -> memory[index] = b.toByte()
        }

        val op = Draw(c, Nibbles(0xd, 0, 0, 5))
        op.run()
        val p = c.frameBuffer.pixel(3, 1)

        val expected = listOf(
                1, 1, 1, 1, 0, 0, 0, 0,
                1, 0, 0, 1, 0, 0, 0, 0,
                1, 1, 1, 1, 0, 0, 0, 0,
                1, 0, 0, 1, 0, 0, 0, 0,
                1, 1, 1, 1, 0, 0, 0, 0)
        val result = readPixels(c, 5, 8)
        assertThat(result).isEqualTo(expected)
        assertThat(cpu.V[0xf]).isEqualTo(0)
        op.run()
        val expected2 = listOf(
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0)

        assertThat(readPixels(c, 5, 8)).isEqualTo(expected2)
        assertThat(cpu.V[0xf]).isEqualTo(1)
        println("")
    }
}