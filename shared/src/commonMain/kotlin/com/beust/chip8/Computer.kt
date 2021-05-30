package com.beust.chip8

import kotlinx.coroutines.*

/**
 * For clients that want to be notified when something happens on the computer.
 */
interface ComputerListener {
    fun onKey(key: Int?)
    fun onPause() {}
    fun onStart() {}
}

class Computer(val display: Display,
        val keyboard: Keyboard = Keyboard(),
        val frameBuffer: FrameBuffer = FrameBuffer(),
        var cpu: Cpu = Cpu(),
        val sound: Boolean = true)
{
    var paused = true
    var cpuClockHz: Long = 1000
        set(v) {
            println("New clock speed: $v")
            field = v
            pause()
            start()
        }

    //private val executor = Executors.newSingleThreadScheduledExecutor()
    private var cpuTickJob: Job? = null
    private var timerFutureJob: Job? = null
    private var romData: ByteArray? = null

    private val scope = MainScope()

    var listener: ComputerListener? = null

    private fun unsigned(b: Byte): Int = if (b < 0) b + 0x10 else b.toInt()


    fun loadRom(romData: ByteArray, launchTimers: Boolean = true) {
        this.romData = romData
        resetCpu()
        if (launchTimers) {
            start()
        }
    }

    private fun resetCpu() {
        cpu = Cpu()
        romData?.let {
            cpu.loadRom(it)
        }
    }

    fun stop() {
        pause()
        display.clear(frameBuffer.frameBuffer)
        resetCpu()
    }

    fun pause() {
        listener?.onPause()
        paused = true
        cpuTickJob?.cancel()
        timerFutureJob?.cancel()
    }

    fun start() {
        listener?.onStart()
        paused = false
        launchTimers()
    }

    private fun nextInstruction(pc: Int = cpu.PC) : Instruction {
        fun extract(pc: Int): Pair<Int, Int> {
            val b = cpu.memory[pc]
            val b0 = unsigned(b.toInt().shr(4).toByte())
            val b1 = unsigned(b.toInt().and(0xf).toByte())
            return Pair(b0, b1)
        }
        val (b0, b1) = extract(pc)
        val (b2, b3) = extract(pc + 1)

        return Instruction(this@Computer, b0, b1, b2, b3)
    }

    private fun launchTimers() {

        cpuTickJob = scope.launch {
            while (true) {
                delay(1000 / cpuClockHz)
                nextInstruction().run()
            }
        }


        timerFutureJob = scope.launch {
            while (true) {
                delay(16)
                if (cpu.DT > 0) {
                    cpu.DT--
                }
                if (cpu.ST > 0) {
                    cpu.ST--
                }
            }
        }
    }

    data class AssemblyLine(val counter: Int, val byte0: Byte, val byte1: Byte, val name: String)

    fun disassemble(p: Int = cpu.PC): List<AssemblyLine> {
        var pc = p
        val result = arrayListOf<AssemblyLine>()
        repeat(30) {
            val inst = nextInstruction(pc)
            result.add(AssemblyLine(pc, cpu.memory[pc], cpu.memory[pc + 1], inst.toString()))
            pc += 2
        }
        return result
    }
}