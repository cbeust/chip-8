package com.beust.chip8

fun log(s: String) {
//    println(s)
}

// Specs: http://www.cs.columbia.edu/~sedwards/classes/2016/4840-spring/designs/Chip8.pdf
// Roms can be found at https://github.com/loktar00/chip8/tree/master/roms

interface KeyListener {
    fun onKey(key: Int?)
}

/** Format to hex with leading zeros */
val Int.h get() = String.format("%02X", this)
val Byte.h get() = String.format("%02X", this)

class Nibbles(private val b0: Int, val b1: Int, val b2: Int, val b3: Int) {
    fun val2(a: Int, b: Int) = a.shl(4) + b
    fun val3(a: Int, b: Int, c: Int) = a.shl(8) + b.shl(4) + c

    override fun toString() = (b0.shl(4) + b1).h + " " + (b2.shl(4) + b3).h
}

class Instruction(private val computer: Computer, b0: Int, b1: Int, b2: Int, b3: Int) {
    private val op: Op

    init {
        val n = Nibbles(b0, b1, b2, b3)
        val undef = Undef(computer, n)
        op = when(b0) {
            0 -> {
                if (b2 == 0xe && b3 == 0) Cls(computer, n) // 00E0
                else if (b2 == 0xe && b3 == 0xe) Ret(computer, n) // 00EE
                else Sys(computer, n) // 0nnn
            }
            1 -> Jmp(computer, n) // 1nnn
            2 -> Call(computer, n)  // 2nnn
            3 -> Se(computer, n) // 3xkk
            4 -> Sne(computer, n) // 4xkk
            5 -> SeVxVy(computer, n) // 5xy0
            6 -> LdV(computer, n) // 6xkk
            7 -> Add(computer, n) // 7xkk
            8 -> {
                when (b3) {
                    0 -> Ld(computer, n) // 8xy0
                    1 -> Or(computer, n) // 8xy1
                    2 -> And(computer, n) // 8xy2
                    3 -> XorVxVy(computer, n) // 8xy3
                    4 -> AddVxVy(computer, n) // 8xy4
                    5 -> SubVxVy(computer, n) // 8xy5
                    6 -> Shr(computer, n) // 8xy6
                    7 -> SubnVxVy(computer, n) // 8xy7
                    0xe -> Shl(computer, n) // 8xyE
                    else -> { undef }
                }
            }
            9 -> SneVxVy(computer, n) // 9xy0
            0xa -> LdI(computer, n) // Annn
            0xb -> JumpV0(computer, n) // Bnnn
            0xc -> Rnd(computer, n) // Ckk
            0xd -> Draw(computer, n) // Dxyn
            0xe -> {
                if (b2 == 9 && b3 == 0xe) SkipIfPressed(computer, n) // Ex9E
                else if (b2 == 0xa && b3 == 0x1) SkipIfNotPressed(computer, n) // ExA1
                else { undef }
            }
            0xf -> {
                if (b2 == 0 && b3 == 7) LdVDt(computer, n) // Fx07
                else if (b2 == 0 && b3 == 0xa) LdVxK(computer, n) // F0A
                else if (b2 == 1 && b3 == 5) LdDt(computer, n) // Fx15
                else if (b2 == 1 && b3 == 8) LdSt(computer, n) // Fx18
                else if (b2 == 1 && b3 == 0xe) AddI(computer, n) // Fx1E
                else if (b2 == 2 && b3 == 9) LdF(computer, n) // Fx29
                else if (b2 == 3 && b3 == 3) LdB(computer, n) // Fx33
                else if (b2 == 5 && b3 == 5) LdIVx(computer, n) // Fx55
                else if (b2 == 6 && b3 == 5) LdVI(computer, n) // Fx65
                else undef
            }
            else -> undef
        }
    }

    override fun toString(): String {
        return op.toString()
    }

    fun run() {
        val cpu = computer.cpu
        log("${cpu.PC.h}: ${op.nib} - $this")
        op.run()
        log("    $cpu")
        cpu.PC += 2
//        cpu.dtCounter++
//        if (cpu.dtCounter % 3000 == 0) {
//            computer.hardware.show()
//            cpu.DT--
//        }
    }
}

