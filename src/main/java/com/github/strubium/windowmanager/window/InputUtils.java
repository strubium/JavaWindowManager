package com.github.strubium.windowmanager.window;

import org.lwjgl.glfw.GLFW;

/**
 * The InputUtils class provides help working with mice
 */
public class InputUtils {

    private final long window;

    /**
     * Constructs a MouseUtils object with the specified window handle.
     *
     * @param window The window handle
     */
    public InputUtils(long window) {
        this.window = window;
    }


    /**
     * Gets the mouse position relative to the window.
     *
     * @return A float array containing [mouseX, mouseY] coordinates
     */
    public float[] getMousePosition() {
        double[] mouseX = new double[1];
        double[] mouseY = new double[1];
        GLFW.glfwGetCursorPos(window, mouseX, mouseY);

        return new float[]{(float) mouseX[0], (float) mouseY[0]};
    }

    /**
     * Converts mouse coordinates to OpenGL coordinates with an offset
     *
     * @param mouseX The x-coordinate of the mouse
     * @param mouseY The y-coordinate of the mouse
     * @param screenWidth The width of the screen or window
     * @param screenHeight The height of the screen or window
     * @return A float array containing [openglMouseX, openglMouseY] coordinates
     */
    public static float[] convertToOpenGLCoordinatesOffset(float mouseX, float mouseY, int screenWidth, int screenHeight, float offsetX, float offsetY) {
        float openglMouseX = mouseX / screenWidth * 2 - 1;
        float openglMouseY = 1 - mouseY / screenHeight * 2;

        return new float[]{openglMouseX + offsetX, openglMouseY + offsetY};
    }

    /**
     * Converts mouse coordinates to OpenGL coordinates.
     *
     * @param mouseX The x-coordinate of the mouse
     * @param mouseY The y-coordinate of the mouse
     * @param screenWidth The width of the screen or window
     * @param screenHeight The height of the screen or window
     * @return A float array containing [openglMouseX, openglMouseY] coordinates
     */
    public static float[] convertToOpenGLCoordinates(float mouseX, float mouseY, int screenWidth, int screenHeight) {
        float openglMouseX = mouseX / screenWidth * 2 - 1;
        float openglMouseY = 1 - mouseY / screenHeight * 2;

        return new float[]{openglMouseX, openglMouseY};
    }

    public boolean isMouseButtonPressed(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyPressed(int button) {
        return GLFW.glfwGetKey(window, button) == GLFW.GLFW_PRESS;
    }

}