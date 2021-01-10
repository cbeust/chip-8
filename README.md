# Chipsk-8
## A [Chip-8](http://www.cs.columbia.edu/~sedwards/classes/2016/4840-spring/designs/Chip8.pdf) emulator written in Kotlin

<p align="center">
    <img src="https://github.com/cbeust/chip-8/blob/master/pics/space-invaders-2.gif?raw=true"/>
</p>

# What is Chip-8?

[Chip-8](https://en.wikipedia.org/wiki/CHIP-8) is a processor developed in the 70s. Because of its simplicity, it's a great starter project for anyone interested in learning how to implement an emulator. The specification is pretty small (about ten pages) and there are several roms available that make it easy to test how well your emulator runs.

# How to run

```
$ ./gradlew run
```

The emulator will load with Space Invaders by default (press 5 to start the game, then 4/5/6 to move around and shoot). Open a new rom by clicking on the "Open rom..." button.

You can pause the emulator at any time (key '`p`'), which will update the disassembly window to show the next instructions about to be executed. You can also adjust the clock speed to make the emulator go slower or faster.

# Architecture

The game creates a [`Computer`](https://github.com/cbeust/chip8/blob/master/src/main/kotlin/com/beust/chip8/Computer.kt) object which is made of a `Display`, `Keyboard`, `FrameBuffer` and `Cpu`.

## Cpu

The CPU reads a new instruction (the next two bytes extracted at the program counter location) at a fixed rate
which defines the clock speed. Two timers are needed: one for the CPU and one for the device timer register,
called `DT`, which needs to tick at 60 Hz according to [the spec](http://www.cs.columbia.edu/~sedwards/classes/2016/4840-spring/designs/Chip8.pdf). Since there is no specific definition for the CPU clock, I used the timing diagram from the document
to set it at around 500Hz:

```kotlin
// CPU clock: around 500 Hz by default
cpuFuture = executor.scheduleAtFixedRate(cpuTick, 0, 1_000_000L / cpuClockHz, TimeUnit.MICROSECONDS)

// Delay Timer: 60 Hz by spec
timerFuture = executor.scheduleAtFixedRate(timerTick, 0, 16L, TimeUnit.MILLISECONDS)
```

<p align="center">
    <img width="50%" src="https://github.com/cbeust/chip-8/blob/master/pics/tetris-1.png?raw=true"/>
</p>

The next two bytes are then masked and turned into instructions. All the op codes can be found in the [Ops.kt](https://github.com/cbeust/chip8/blob/master/src/main/kotlin/com/beust/chip8/Ops.kt) file. Here is an example:

```kotlin
/**
 * 7xkk
 * Set Vx = Vx + kk
 */
class Add(c: Computer, n: Nibbles): Op(c, n) {
    override fun run() { cpu.V[x] = unsigned(cpu.V[x] + kk) }
    override fun toString() = "ADD V$x, $kk"
}
```

## Display

The `Display` is a simple interface which allows multiple strategies to render the frame buffer:

```kotlin
interface Display {
    val pane: Pane
    fun draw(frameBuffer: IntArray)
    fun clear(frameBuffer: IntArray)
}
```

For example, here is a text based renderer:

<p align="center">
    <img width="50%" src="https://github.com/cbeust/chip-8/blob/master/pics/space-invaders-text.png?raw=true"/>
</p>
    
The emulator window will resize gracefully:

<p align="center">
    <img width="20%" src="https://github.com/cbeust/chip-8/blob/master/pics/space-invaders-small.png?raw=true"/>
</p>

You can also easily alter other aspects of the renderer:

<p align="center">
    <img width="50%" src="https://github.com/cbeust/chip-8/blob/master/pics/space-invaders-colors.png?raw=true"/>
</p>

