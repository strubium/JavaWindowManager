package com.github.strubium.windowmanager.window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.stb.STBImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Deals with the creation and destruction of the window
 *
 * @author strubium
 */
public class WindowManager {

    /**The window handle*/
    public long window;  //
    private boolean fullscreen;
    private final int windowWidth;
    private final int windowHeight;


    /**
     * Create a WindowManager
     *
     * @param width The width of the window
     * @param height The height of the window
     * @param fullscreen Should the window be in fullscreen or windowed
     */
    public WindowManager(int width, int height, boolean fullscreen) {
        this.windowWidth = width;
        this.windowHeight = height;
        this.fullscreen = fullscreen;
    }


    /**
     * Create the Window
     *
     * @param windowTitle The title of the window to use
     * @param vSync Should it use vSync?
     */
    public void createWindow(String windowTitle, boolean vSync) {
        // Setup error callback
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure window settings
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);  // Window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);  // Window will be resizable

        long monitor = fullscreen ? glfwGetPrimaryMonitor() : NULL;
        window = glfwCreateWindow(windowWidth, windowHeight, windowTitle, monitor, NULL);

        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        centerWindow();

        glfwMakeContextCurrent(window);
        glfwSwapInterval(vSync ? 1 : 0); // Enable v-sync
        glfwShowWindow(window);

        GL.createCapabilities(); // This line is critical for LWJGL's interoperation with GLFW's OpenGL context

        doOpenGLSetup();
    }

    private void centerWindow() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if (vidMode != null) {
                glfwSetWindowPos(
                        window,
                        (vidMode.width() - pWidth.get(0)) / 2,
                        (vidMode.height() - pHeight.get(0)) / 2
                );
            }
        }
    }

    /**
     * Sets up the default key callback for a window
     */
    public void setupDefaultKeys(){
        // Set up key callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key == GLFW_KEY_F11 && action == GLFW_PRESS) {
                toggleFullscreen();
            }
        });
    }

    /**
     * Toggles fullscreen for the window
     */
    public void toggleFullscreen() {
        fullscreen = !fullscreen;

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (fullscreen) {
            glfwSetWindowMonitor(window, glfwGetPrimaryMonitor(), 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
        } else {
            glfwSetWindowMonitor(window, NULL, (vidMode.width() - windowWidth) / 2, (vidMode.height() - windowHeight) / 2, windowWidth, windowHeight, GLFW_DONT_CARE);
        }
    }

    /**
     * Should the window close?
     *
     * @return true if the window should close
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    private void pollEvents() {
        glfwPollEvents();
    }

    private void swapBuffers() {
        glfwSwapBuffers(window);
    }

    /**
     * Swap buffers and Poll events
     */
    public void swapAndPoll(){
        swapBuffers();
        pollEvents();
    }

    /**
     * Do the basic OpenGl setup for this window
     */
    private void doOpenGLSetup(){
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK); // Cull back faces
    }

    /**
     * Is the window focused?
     *
     * @return true if focused, false if not
     */
    public boolean isFocused(){
        return glfwGetWindowAttrib(window, GLFW_FOCUSED) != 0;
    }

    /**
     * Destroy the window
     */
    public void destroy() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Change the title of a window
     *
     * @param newTitle The title to change to
     */
    public void setWindowTitle(String newTitle) {
        glfwSetWindowTitle(window, newTitle);
    }

    /**
     * Set the window icon
     *
     * @param imagePath The path to the icon image (must be PNG or similar)
     */
    public void setWindowIcon(String imagePath) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer image = STBImage.stbi_load(imagePath, width, height, channels, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load icon image: " + STBImage.stbi_failure_reason());
            }

            GLFWImage.Buffer iconBuffer = GLFWImage.malloc(1, stack);
            iconBuffer.position(0);
            iconBuffer.width(width.get(0));
            iconBuffer.height(height.get(0));
            iconBuffer.pixels(image);

            glfwSetWindowIcon(window, iconBuffer);

            STBImage.stbi_image_free(image);
        }
    }

}