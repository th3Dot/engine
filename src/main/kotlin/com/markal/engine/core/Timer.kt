package com.markal.engine.core

import org.lwjgl.glfw.GLFW

class Timer {

    var fps = 0
        private set
    var ups = 0
        private set
    val time
        get() = GLFW.glfwGetTime()


    private var timeCount = 0f
    private var fpsCount = 0
    private var upsCount = 0
    private var lastUpdate = time

    fun getDelta(): Float {
        val time = time
        val delta = time.toFloat() - lastUpdate.toFloat()
        lastUpdate = time
        timeCount += delta
        return delta
    }

    fun updateFps() {
        fpsCount++
    }

    fun updateUps() {
        upsCount++
    }

    fun update() {
        if (timeCount >= 1f) {
            fps = fpsCount
            ups = upsCount

            fpsCount = 0
            upsCount = 0

            timeCount -= 1f

            System.out.println("FPS: $fps | UPS: $ups")
        }
    }
}
