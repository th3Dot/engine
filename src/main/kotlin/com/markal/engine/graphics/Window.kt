package com.markal.engine.graphics

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil

/**
 * This class represents a GLFW window.
 *
 * @author Heiko Brumme
 */
class Window
/**
 * Creates a GLFW window and its OpenGL context with the specified width,
 * height and title.
 *
 * @param width  Width of the drawing area
 * @param height Height of the drawing area
 * @param title  Title of the window
 * @param vsync  Set to true, if you want v-sync
 */
constructor(width: Int, height: Int, title: CharSequence, vsync: Boolean) {

    /**
     * Stores the window handle.
     */
    private val id: Long

    /**
     * Key callback for the window.
     */
    private val keyCallback: GLFWKeyCallback

    /**
     * Shows if vsync is enabled.
     */
    /**
     * Check if v-sync is enabled.
     *
     * @return true if v-sync is enabled
     */
    var isVSyncEnabled: Boolean = false
        private set

    /**
     * Returns if the window is closing.
     *
     * @return true if the window should close, else false
     */
    val isClosing: Boolean
        get() = GLFW.glfwWindowShouldClose(id)

    init {
        this.isVSyncEnabled = vsync

        /* Creating a temporary window for getting the available OpenGL version */
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        val temp = GLFW.glfwCreateWindow(1, 1, "", MemoryUtil.NULL, MemoryUtil.NULL)
        GLFW.glfwMakeContextCurrent(temp)
        GL.createCapabilities()
        val caps = GL.getCapabilities()
        GLFW.glfwDestroyWindow(temp)

        /* Reset and set window hints */
        GLFW.glfwDefaultWindowHints()
        if (caps.OpenGL32) {
            /* Hints for OpenGL 3.2 core profile */
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
        } else if (caps.OpenGL21) {
            /* Hints for legacy OpenGL 2.1 */
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2)
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1)
        } else {
            throw RuntimeException("Neither OpenGL 3.2 nor OpenGL 2.1 is " + "supported, you may want to update your graphics driver.")
        }
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)

        /* Create window with specified OpenGL context */
        id = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (id == MemoryUtil.NULL) {
            GLFW.glfwTerminate()
            throw RuntimeException("Failed to create the GLFW window!")
        }

        /* Center window on screen */
        val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
        GLFW.glfwSetWindowPos(id,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        )

        /* Create OpenGL context */
        GLFW.glfwMakeContextCurrent(id)
        GL.createCapabilities()

        /* Enable v-sync */
        if (vsync) {
            GLFW.glfwSwapInterval(1)
        }

        /* Set key callback */
        keyCallback = object : GLFWKeyCallback() {
            override operator fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                    GLFW.glfwSetWindowShouldClose(window, true)
                }
            }
        }
        GLFW.glfwSetKeyCallback(id, keyCallback)
    }

    /**
     * Sets the window title
     *
     * @param title New window title
     */
    fun setTitle(title: CharSequence) {
        GLFW.glfwSetWindowTitle(id, title)
    }

    /**
     * Updates the screen.
     */
    fun update() {
        GLFW.glfwSwapBuffers(id)
        GLFW.glfwPollEvents()
    }

    /**
     * Destroys the window an releases its callbacks.
     */
    fun destroy() {
        GLFW.glfwDestroyWindow(id)
        keyCallback.free()
    }

    /**
     * Setter for v-sync.
     *
     * @param vsync Set to true to enable v-sync
     */
    fun setVSync(vsync: Boolean) {
        this.isVSyncEnabled = vsync
        if (vsync) {
            GLFW.glfwSwapInterval(1)
        } else {
            GLFW.glfwSwapInterval(0)
        }
    }
}
