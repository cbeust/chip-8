package com.beust.chip8

import kotlin.random.Random

/**
 * An Op is made of two bytes which are sliced into four nibbles. These nibbles are then used
 * to determine the op and its data. Bytes get sliced differently depending on the opcode. The base
 * class offers some convenience functions to access these nibbles but makes them lazy since ops
 * will need different ones and we don't want to do unnecessary work.
 */
sealed class Op(val computer: Computer, val nib: Nibbles) {
    protected val nnn by lazy { nib.val3(nib.b1, nib.b2, nib.b3) }
    protected val x by lazy { nib.b1 }
    protected val y by lazy { nib.b2 }
    protected val n by lazy { nib.b3 }
    protected val kk by lazy { nib.val2(nib.b2, nib.b3) }
    protected val cpu = computer.cpu

    open fun run() {
        TODO("  NOT IMPLEMENTED $nib")
        println("")
    }

    protected fun unsigned(n: Int): Int = (n + 0x100) and 0xff
}

/**
 * 00E0
 * Clear screen
 */
class Cls(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        computer.frameBuffer.clear()
        computer.display.draw(computer.frameBuffer.frameBuffer)
    }
    override fun toString() = "CLS"
}

/**
 * 00EE
 * Return
 */
class Ret(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.PC = cpu.SP.pop() }
    override fun toString()= "RET"
}

/**
 * 0nnn
 * SYS addr (not implemented)
 */
class Sys(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.PC -= 2 }  // loop in place if we reach here
    override fun toString()= "SYS ${nnn.h}"
}

/**
 * 1nnn
 * Jump to address
 */
class Jmp(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.PC = nnn - 2 }
    override fun toString() = "JMP ${nnn.h}"
}

/**
 *  2nnn
 *  Call, jump to subroutine
 */
class Call(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        cpu.SP.push(cpu.PC)
        cpu.PC = nnn - 2
    }
    override fun toString() = "CALL ${nnn.h}"
}

/**
 *  3xkk
 *  Skip next instruction if Vx = kk
 */
class Se(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { if (cpu.V[x] == kk) cpu.PC += 2 }
    override fun toString() = "SE V$x, $kk"
}

/**
 *  4xkk
 *  Skip next instruction if Vx != kk
 */
class Sne(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { if (cpu.V[x] != kk) cpu.PC += 2 }
    override fun toString() = "SNE V$x, $kk"
}

/**
 *  5xy0
 *  Skip next instruction if Vx = Vy
 */
class SeVxVy(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { if (cpu.V[x] == cpu.V[y]) cpu.PC += 2 }
    override fun toString() = "SE V$x, V$y"
}

/**
 * 6xkk
 * Set Vx = kk.
 */
class LdV(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { computer.cpu.V[x] = kk }
    override fun toString() = "LD V$x, $kk"
}

/**
 * 7xkk
 * Set Vx = Vx + kk
 */
class Add(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.V[x] = unsigned(cpu.V[x] + kk) }
    override fun toString() = "ADD V$x, $kk"
}

/**
 * 8xy0
 * Set Vx = Vy
 */
class Ld(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.V[x] = cpu.V[y] }
    override fun toString() = "LD V$x, V$y"
}

/**
 * 8xy1
 * Set Vx = Vx OR Vy.
 */
class Or(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.V[x] = cpu.V[x] or cpu.V[y] }
    override fun toString() = "OR V$x, V$y"
}

/**
 * 8xy2
 * Set Vx = Vx AND Vy.
 */
class And(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.V[x] = cpu.V[x] and cpu.V[y] }
    override fun toString() = "AND V$x, V$y"
}

/**
 * 8xy3
 * Set Vx = Vx XOR Vy
 */
class XorVxVy(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.V[x] = cpu.V[x] xor cpu.V[y] }
    override fun toString() = "XOR V$x, V$y"
}

/**
 * 8xy4
 * Set Vx = Vx + Vy, set VF = carry.
 */
class AddVxVy(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        val newValue = cpu.V[x] + cpu.V[y]
        cpu.V[0xf] = if (newValue > 0xff) 1 else 0
        cpu.V[x] = unsigned(newValue)
    }
    override fun toString() = "ADD V$x, V$y"
}

/**
 * 8xy5
 * Set Vx = Vx - Vy, set VF = NOT borrow.
 */
class SubVxVy(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        val newValue = cpu.V[x] - cpu.V[y]
        cpu.V[0xf] = if (newValue < 0) 0 else 1
        cpu.V[x] = unsigned(newValue)
    }
    override fun toString() = "SUB V$x, V$y"
}

/**
 * 8xy6
 * Set Vx = Vx SHR 1. I
 */
class Shr(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        cpu.V[0xf] = cpu.V[x] % 2
        cpu.V[x] = cpu.V[x] shr 1
    }
    override fun toString() = "SHR V$x"
}

/**
 * 8xy7
 * Set Vx = Vy - Vx, set VF = NOT borrow.
 */
class SubnVxVy(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        val newValue = cpu.V[y] - cpu.V[x]
        cpu.V[0xf] = if (newValue < 0) 0 else 1
        cpu.V[x] = unsigned(newValue)
    }
    override fun toString() = "SBUN V$x,V$y"
}


/**
 * 8xyE
 * Set Vx = Vx SHL 1. I
 */
class Shl(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        cpu.V[0xf] = cpu.V[x] % 2
        cpu.V[x] = cpu.V[x] shl 1
    }
    override fun toString() = "SHL V$x"
}

/**
 * 9xy0
 * Skip next instruction if Vx != Vy.@*/
class SneVxVy(c: Computer, n: Nibbles): SkipBase(c, n) {
    override fun condition(key: Int?, expected: Int) = cpu.V[x] != cpu.V[y]
    override fun toString() = "SNE V$x, V$y"
}

/**
 * Annn
 * Set I = nnn.
 */
class LdI(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.I = nnn }
    override fun toString() = "LD I, ${nnn.h}"
}

/**
 * Bnnn
 * Jump to location nnn + V0. The program counter is set to nnn plus the value of V0.
 */
class JumpV0(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.PC = nnn + cpu.V[0] }
    override fun toString() = "JUMP V0"
}

/**
 * Cxkk
 * Set Vx = random byte AND kk.
 */
class Rnd(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.V[x] = Random.nextInt() and kk }
    override fun toString() = "RND V[$x], $kk"
}

/**
 * Dxyn
 * Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
 */
class Draw(c: Computer, nib: Nibbles): Op(c, nib) {
    override fun run() {
        cpu.V[0xf] = 0
        repeat(n) { byte ->
            val yy = (cpu.V[y] + byte) % FrameBuffer.HEIGHT
            val sprite = cpu.memory[cpu.I + byte].toInt()
            repeat(8) { bit ->
                val xx = (cpu.V[x] + bit) % FrameBuffer.WIDTH
                val color = (sprite shr (7 - bit)) and 1
                val oldValue = computer.frameBuffer.pixel(xx, yy)
                val newValue = oldValue xor color
                cpu.V[0xf] = cpu.V[0xf] or (color and oldValue)
                computer.frameBuffer.setPixel(xx, yy, newValue)
            }
        }
        computer.display.draw(computer.frameBuffer.frameBuffer)
    }
    override fun toString() = "DRAW V$x, V$y, ${n.h}"
}

/**
 * Base class for the two ops that deal with key presses: SKP and SKNP.
 */
abstract class SkipBase(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        val key = computer.keyboard.key
        val expected = computer.cpu.V[x]
        if (condition(key, expected)) {
            computer.cpu.PC += 2
        }
    }

    abstract fun condition(key: Int?, expected: Int): Boolean
}

/**
 * Ex9E
 * Skip if key is pressed
 */
class SkipIfPressed(c: Computer, n: Nibbles): SkipBase(c, n) {
    override fun condition(key: Int?, expected: Int) = key != null && key == expected
    override fun toString() = "SKP V$x"
}

/**
 * ExA1
 * Skip if key is not pressed
 */
class SkipIfNotPressed(c: Computer, n: Nibbles): SkipBase(c, n) {
    override fun condition(key: Int?, expected: Int) = key == null || key != expected
    override fun toString() = "SKNP V$x"
}

/**
 * Fx07
 * Set Vx = delay timer value.
 */
class LdVDt(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.V[x] = cpu.DT }
    override fun toString() = "LD V$x, DT"
}

/**
 * Fx0a
 * Wait for a key press, store the value of the key in Vx
 */
class LdVxK(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { computer.cpu.V[x] = computer.keyboard.waitForKeyPress() }
    override fun toString() = "LD V$x, Keyboard"
}

/**
 * Fx15
 * Set delay timer = Vx.
 */
class LdDt(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.DT = cpu.V[x] }
    override fun toString() = "LD DT, V$x"
}

/**
 * Fx18
 * Set sound timer = Vx.
 */
class LdSt(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.ST = cpu.V[x] }
    override fun toString() = "LD ST, V$x"
}

/**
 * Fx1E
 * Set I = I + Vx.
 */
class AddI(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.I += cpu.V[x] }
    override fun toString() = "ADD I, V$x"
}

/**
 * Fx29
 * Set I = location of sprite for digit Vx.
 */
class LdF(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.I = cpu.V[x] * 5 }
    override fun toString() = "LD F, V$x"
}

/**
 * Fx33
 * Store BCD representation of Vx in memory locations I, I+1, and I+2.
 */
class LdB(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        val v = cpu.V[x]
        cpu.memory[cpu.I] = (v / 100).toByte()
        cpu.memory[cpu.I + 1] = ((v % 100) / 10).toByte()
        cpu.memory[cpu.I + 2] = (v % 10).toByte()
    }
    override fun toString() = "LD B, V$x"
}

/**
 * Fx55
 * Stores V0 to VX in memory starting at address I. I is then set to I + x + 1.
 */
class LdIVx(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        repeat(x + 1) {
            cpu.memory[cpu.I + it] = cpu.V[it].toByte()
        }
        cpu.I += x + 1
    }
    override fun toString() = "LD [I], V$x"
}

/**
 * Fx65
 * Fills V0 to VX with values from memory starting at address I.
 */
class LdVI(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() {
        repeat(x + 1) {
            cpu.V[it] = unsigned(cpu.memory[cpu.I + it].toInt())
        }
        cpu.I += x + 1
    }
    override fun toString() = "LD V$x, [I]"
}

class Undef(c: Computer, nib: Nibbles): Op(c, nib) {
    override fun toString() = "Undef($n)"
}
