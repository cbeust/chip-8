package com.beust.chip8

class Keyboard {
    private val keySemaphore = Object()
    var key: Int? = null
        set(v) {
            synchronized(keySemaphore) {
                field = v
                keySemaphore.notify()
            }
        }

    fun waitForKeyPress(): Int {
        return if (key != null) key!!
        else {
            synchronized(keySemaphore) {
                keySemaphore.wait()
                key!!
            }
        }
    }
}