package com.beust.chip8

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.net.URL
import java.nio.file.Paths
import java.util.*
import kotlin.system.exitProcess

class Controller : Initializable {
    var currentRomPath: SimpleStringProperty? = null
    var computer: Computer? = null
    var pauseButton: Button? = null
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        println("Initializing controller")
    }
}

class MyFxApp : Application() {
    private val computer = Computer()
    private var pauseButton: Button? = null
    private var disassembly: GridPane? = null
    private val isPaused = SimpleBooleanProperty().apply {
        addListener { _, _, newVal -> computer.apply {
            // Computer is paused

            // Update the pause button label
            pauseButton?.text = if (newVal)  "_Resume" else "_Pause"

            // Display the disassembly
            updateDisassembly(disassembly!!, computer.disassemble())
        }
    }}

    /**
     * Display the disassembly.
     */
    private fun updateDisassembly(disassembly: GridPane, listing: List<Computer.AssemblyLine>) {
        disassembly.children.clear()
        listing.forEachIndexed() { row, al ->
            var column = 0
            Label(al.counter.h).let { label ->
                GridPane.setConstraints(label, column++, row)
                disassembly.children.add(label)
            }
            Label(al.byte0.h + " " + al.byte1.h).let { label ->
                GridPane.setConstraints(label, column++, row)
                disassembly.children.add(label)
            }
            Label(al.name).let { label ->
                GridPane.setConstraints(label, column++, row)
                disassembly.children.add(label)
            }
        }
    }

    private val currentRomName = SimpleStringProperty()
    private val currentRomPath = SimpleStringProperty().apply {
        addListener { _, _, newVal -> computer.apply {
            // Restart computer
            stop()
            loadRom(File(newVal))

            // Update the rom name
            val start = newVal.lastIndexOf(File.separatorChar)
            val end = newVal.lastIndexOf(".ch8")
            val romName = if (end == -1) newVal.substring(start + 1)
                else newVal.substring(start + 1, end)
            currentRomName.set(romName)
        }}
    }

    override fun start(primaryStage: Stage) {
        val keyListener = object: KeyListener {
            override fun onKey(key: Int?) {
                computer.keyboard.key = key
            }
        }

        primaryStage.title = "CHIP-8"
        primaryStage.onCloseRequest = EventHandler {
            Platform.exit()
            exitProcess(0)
        }
        val url = this::class.java.classLoader.getResource("main.fxml")
        val loader = FXMLLoader(url)
        val res = url.openStream()
        val root = loader.load<AnchorPane>(res)

        pauseButton = root.lookup("#pause") as Button
        val scrollPane = root.lookup("#disassemblyScrollPane") as ScrollPane
        disassembly = scrollPane.content as GridPane
        val controller = loader.getController<Controller>()

        controller.let {
            it.currentRomPath = currentRomPath
            it.computer = computer
            it.pauseButton = pauseButton
        }

        computer.listener = object: ComputerListener {
            override fun onKey(key: Int?) {
                computer.keyboard.key = key
            }

            override fun onPause() {
                isPaused.set(true)
            }

            override fun onStart() {
                isPaused.set(false)
            }
        }

        val scene = Scene(root)
        val bp = root.lookup("#emulator") as VBox
        bp.children.add(computer.display.pane)
        primaryStage.scene = scene
        primaryStage.show()

        // Load rom button
        (root.lookup("#loadRom") as Button).setOnAction {
            computer.pause()
            val romFile = FileChooser().run {
                initialDirectory = File("roms")
                showOpenDialog(Stage())
            }
            println("Picked file $romFile")
            if (romFile != null) {
                currentRomPath.set(romFile.absolutePath)
            }
        }

        // Pause button
        pauseButton?.setOnAction {
            if (computer.paused) {
                computer.start()
            } else {
                computer.pause()
            }
        }

        // CPU clock button
        with((root.lookup("#cpuClock") as TextField)) {
            fun updateClock() {
                computer.cpuClockHz = text.toLong()
            }
            text = computer.cpuClockHz.toString()
            onAction = EventHandler { e ->
                updateClock()
            }
            focusedProperty().addListener { _, _, onFocus ->
                if (!onFocus) {
                    updateClock()
                }
            }
        }

        // Current ROM
        val currentRomLabel = root.lookup("#currentRom") as Label
        currentRomLabel.textProperty().bind(currentRomName)
        scene.setOnKeyPressed { event: KeyEvent ->
            when(event.code) {
                KeyCode.Q -> {
                    primaryStage.close()
                    computer.stop()
                    exitProcess(0)
                }
                KeyCode.P -> {
                    if (computer.paused) {
                        computer.start()
                    } else {
                        computer.pause()
                    }
                }
                else -> {
                    if (computer.paused && event.code != KeyCode.ALT) computer.start()
                    else {
                        try {
                            val key = Integer.parseInt(event.code.char, 16)
                            keyListener.onKey(key)
                        } catch (ex: NumberFormatException) {
                            println("Can't parse key " + event.code.char + ", ignoring")
                        }
                    }
                }
            }
        }
        scene.setOnKeyReleased { event: KeyEvent ->
            keyListener.onKey(null)
        }

        val spaceInvaders = Paths.get("roms", "Space Invaders [David Winter].ch8")
        val breakout = Paths.get("roms", "Breakout [Carmelo Cortez, 1979].ch8")
        val file3 = Paths.get("roms", "Tetris [Fran Dachille, 1991].ch8")
        val file4 = Paths.get("roms", "Clock Program [Bill Fisher, 1981].ch8")
        val file5 = Paths.get("roms", "IBM Logo.ch8")
        val keypad = Paths.get("roms", "Keypad Test [Hap, 2006].ch8")
        currentRomPath.set(spaceInvaders.toFile().absolutePath)
//        val rom = file2.toFile() // if (arg.rom != null) File(arg.rom) else file3.toFile()
//        chip8.run(rom)
    }
}

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        class Arg {
            @Parameter(names = arrayOf("--rom"))
            var rom: String? = null
            @Parameter(names = arrayOf("--start"))
            var start: String? = "200"
        }
        val arg = Arg()
        JCommander.newBuilder().addObject(arg).args(args).build()
        val computer = Computer(sound = false)
        val start = Integer.parseInt(arg.start!!, 16)
        val listing = computer.disassemble(start)
        println("=== " + arg.rom + " PC=0x" + Integer.toHexString(start))
        listing.forEach { println(it) }
    } else {
        Application.launch(MyFxApp::class.java)
    }
}

