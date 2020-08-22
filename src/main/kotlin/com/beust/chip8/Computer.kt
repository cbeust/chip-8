package com.beust.chip8

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

/**
 * For clients that want to be notified when something happens on the computer.
 */
interface ComputerListener {
    fun onKey(key: Int?)
    fun onPause() {}
    fun onStart() {}
}

class Computer(val display: Display = DisplayGraphics(),
        val keyboard: Keyboard = Keyboard(),
        val frameBuffer: FrameBuffer = FrameBuffer(),
        var cpu: Cpu = Cpu(),
        val sound: Boolean = true)
{
    var paused = true
    var cpuClockHz: Long = 500
        set(v) {
            println("New clock speed: $v")
            field = v
            pause()
            start()
        }

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var cpuFuture: ScheduledFuture<*>? = null
    private var timerFuture: ScheduledFuture<*>? = null
    private var romFile: File? = null
    private val soundInputStream by lazy {
        AudioSystem.getAudioInputStream(
                this::class.java.classLoader.getResource("sound.wav"))
    }
    private val clip by lazy { AudioSystem.getClip().apply {
        open(soundInputStream)
    }}

    var listener: ComputerListener? = null

    private fun unsigned(b: Byte): Int = if (b < 0) b + 0x10 else b.toInt()

    fun loadRom(romFile: File, launchTimers: Boolean = true) {
        this.romFile = romFile
        resetCpu()
        if (launchTimers) {
            start()
        }
    }

    private fun resetCpu() {
        cpu = Cpu()
        romFile?.let {
            cpu.loadRom(it.readBytes())
        }
    }

    fun stop() {
        pause()
        clip.stop()
        display.clear(frameBuffer.frameBuffer)
        resetCpu()
    }

    fun pause() {
        listener?.onPause()
        paused = true
        cpuFuture?.cancel(true)
        timerFuture?.cancel(true)
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
        val cpuTick = Runnable {
            nextInstruction().run()
        }

        val timerTick = Runnable {
            if (cpu.DT > 0) {
                cpu.DT--
            }
            if (cpu.ST > 0) {
                if (sound && ! clip.isActive) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY)
                }
                cpu.ST--
            } else {
                clip.stop()
            }
        }

        cpuFuture = executor.scheduleAtFixedRate(cpuTick, 0, 1_000_000L / cpuClockHz, TimeUnit.MICROSECONDS)
        timerFuture = executor.scheduleAtFixedRate(timerTick, 0, 16L, TimeUnit.MILLISECONDS)
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