package com.beust.chip8

import java.util.*

@Suppress("PropertyName")
class Cpu {
    private val PC_START = 0x200

    /** Program counter */
    var PC: Int = PC_START

    /** V registers */
    val V: IntArray = IntArray(16)

    /** I register */
    var I: Int = 0

    /** Delay timer */
    var DT: Int = 0

    /** Sound timer */
    var ST: Int = 0

    /** Stack pointer */
    val SP = Stack<Int>()

    val memory = ByteArray(4096)

    /** The sprites are in the ROM at address 0x000 - 0x080 */
    private val FONT_SPRITES = arrayOf(
        0xf0, 0x90, 0x90, 0x90, 0xf0, // 8
        0x20, 0x60, 0x20, 0x20, 0x70, // 9
        0xf0, 0x10, 0xf0, 0x80, 0xf0, // 0
        0xf0, 0x10, 0xf0, 0x10, 0xf0, // 1
        0x90, 0x90, 0xf0, 0x10, 0x10, // A
        0xf0, 0x80, 0xf0, 0x10, 0xf0, // B
        0xf0, 0x80, 0xf0, 0x90, 0xf0, // 2
        0xf0, 0x10, 0x20, 0x40, 0x40, // 3
        0xf0, 0x90, 0xf0, 0x90, 0xf0, // C
        0xf0, 0x90, 0xf0, 0x10, 0xf0, // D
        0xf0, 0x90, 0xf0, 0x90, 0x90, // 4
        0xe0, 0x90, 0xe0, 0x90, 0xe0, // 5
        0xf0, 0x80, 0x80, 0x80, 0xf0, // E
        0xe0, 0x90, 0x90, 0x90, 0xe0, // F
        0xf0, 0x80, 0xf0, 0x80, 0xf0, // 6
        0xf0, 0x80, 0xf0, 0x80, 0x80  // 7
    )

    init {
        // Move the sprites at adress 0x000
        FONT_SPRITES.forEachIndexed { index, v ->
            memory[index] = v.toByte()
        }
    }

    fun loadRom(romBytes: ByteArray = ByteArray(4096)) {
        // Load the rom at 0x200
        romBytes.copyInto(memory, 0x200)
    }

    override fun toString(): String {
        val vs = V.filter { it != 0 }.mapIndexed { ind, v -> "v$ind=$v" }
        val sp = SP.map { it.h }
        return "{Cpu pc=${Integer.toHexString(PC)} i=${I.h} dt=${DT.h} st=${ST.h} sp=${sp} $vs}"
    }
}