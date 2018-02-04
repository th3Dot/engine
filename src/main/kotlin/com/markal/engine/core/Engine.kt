package com.markal.engine.core

import com.markal.engine.`object`.Triangle
import com.markal.engine.graphics.Window
import com.markal.engine.math.Matrix4f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import org.lwjgl.opengl.GL20.glLinkProgram
import org.lwjgl.opengl.GL20.glAttachShader
import org.lwjgl.opengl.GL20.glCreateProgram
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import java.nio.FloatBuffer






fun main(args: Array<String>) {
    Engine().start()
}

val triangle = Triangle(listOf(0f to 0f, 1f to 0f, 0.5f to 1f))

class Engine {

    private val targetUps = 1
    private val errorCallback: GLFWErrorCallback = GLFWErrorCallback.createPrint(System.err)
    private val timer = Timer()
    private lateinit var window: Window
    private var running = false

    private var uniModel: Int = 0
    private var angle = 0f
    private val anglePerSecond = 50f
    private var previousAngle = 0f

    private fun init() {
        glfwSetErrorCallback(errorCallback)

        if (!glfwInit()) {
            throw IllegalStateException("GLFW could not be initialized.")
        }

        window = Window(800, 600, "Main Window", false)

        running = true
    }

    fun start() {
        init()
        gameLoop()
        dispose()
    }

    private fun gameLoop() {
        var delta: Float
        var accumulator = 0f
        val interval = 1f / targetUps
        var alpha: Float

        while (running) {
            if (window.isClosing) running = false

            delta = timer.getDelta()
            accumulator += delta

            input()

            while (accumulator >= interval) {
                update(1f / targetUps)
                timer.updateUps()
                accumulator -= interval
            }

            alpha = accumulator / interval

            initRenderProgram()

            render(alpha)
            timer.updateFps()

            timer.update()
            window.update()
        }
    }

    private fun dispose() {
        window.destroy()

        glfwTerminate()
        errorCallback.free()
    }

    private fun input() {

    }

    private fun update(updateInterval: Float) {
        previousAngle = angle
        angle += updateInterval * anglePerSecond;

    }

    private fun render(alpha: Float) {
        glClear(GL_COLOR_BUFFER_BIT);

        val lerpAngle = (1f - alpha) * previousAngle + alpha * angle
        val model = Matrix4f.rotate(lerpAngle, 0f, 0f, 1f)
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(4*4)
            model.toBuffer(buffer)
            glUniformMatrix4fv(uniModel, false, buffer)
        }

        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private fun initRenderProgram() {
        val vao = glGenVertexArrays()
        glBindVertexArray(vao)

        MemoryStack.stackPush().use { stack ->
            val vertices = stack.mallocFloat(3 * 6)
            vertices.put(-0.6f).put(-0.4f).put(0f).put(1f).put(0f).put(0f)
            vertices.put(0.6f).put(-0.4f).put(0f).put(0f).put(1f).put(0f)
            vertices.put(0f).put(0.6f).put(0f).put(0f).put(0f).put(1f)
            vertices.flip()

            val vbo = glGenBuffers()
            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        }


        val vertexSource = loadShaderSource(javaClass.classLoader.getResource("default.vert").file)
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, vertexSource)
        glCompileShader(vertexShader)

        val fragmentSource = loadShaderSource(javaClass.classLoader.getResource("default.frag").file)
        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fragmentSource)
        glCompileShader(fragmentShader)

        val shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glBindFragDataLocation(shaderProgram, 0, "fragColor")
        glLinkProgram(shaderProgram)

        glUseProgram(shaderProgram);

        val floatSize = 4

        val posAttrib = glGetAttribLocation(shaderProgram, "position")
        glEnableVertexAttribArray(posAttrib)
        glVertexAttribPointer(posAttrib, 3, GL_FLOAT, false, 6 * floatSize, 0L)

        val colAttrib = glGetAttribLocation(shaderProgram, "color")
        glEnableVertexAttribArray(colAttrib)
        glVertexAttribPointer(colAttrib, 3, GL_FLOAT, false, 6 * floatSize, 3L * floatSize)

        uniModel = glGetUniformLocation(shaderProgram, "model")
        val model = Matrix4f()
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(4*4)
            model.toBuffer(buffer)
            glUniformMatrix4fv(uniModel, false, buffer)
        }


        val uniView = glGetUniformLocation(shaderProgram, "view")
        val view = Matrix4f()
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(4*4)
            view.toBuffer(buffer)
            glUniformMatrix4fv(uniView, false, buffer)
        }

        val uniProjection = glGetUniformLocation(shaderProgram, "projection")
        val ratio = 640f / 480f
        val projection = Matrix4f.orthographic(-ratio, ratio, -1f, 1f, -1f, 1f)
        MemoryStack.stackPush().use { stack ->
            val buffer = stack.mallocFloat(4*4)
            projection.toBuffer(buffer)
            glUniformMatrix4fv(uniProjection, false, buffer)
        }
    }

    private fun loadShaderSource(path: String): String {
        val builder = StringBuilder()

        try {
            FileInputStream(path).use { input ->
                BufferedReader(InputStreamReader(input)).use { reader ->
                    while (true) {
                        reader.readLine()?.let { line ->
                            builder.append(line).append("\n")
                        } ?: break
                    }
                }
            }
        } catch (ex: IOException) {
            throw RuntimeException("Failed to load a shader file!"
                    + System.lineSeparator() + ex.message)
        }

        return builder.toString()
    }
}
