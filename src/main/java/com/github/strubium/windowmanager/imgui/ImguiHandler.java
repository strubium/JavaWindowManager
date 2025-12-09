package com.github.strubium.windowmanager.imgui;

import com.github.strubium.windowmanager.window.WindowManager;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

/**
 * Handler class for integrating ImGui with GLFW and OpenGL.
 * Provides initialization, input handling, and rendering for ImGui.
 *
 * @author strubium
 */
public class ImguiHandler {
    private ImGuiImplGl3 imguiGl3;
    private ImGuiImplGlfw imguiGlfw;
    private final WindowManager windowManager;


    /**
     * Constructor for ImguiHandler.
     *
     * @param window the GLFW window handle.
     */
    public ImguiHandler(WindowManager window) {
        this.windowManager = window;
    }

    /**
     * Initializes ImGui and sets up OpenGL bindings.
     *
     * @param glslVersion the version of OpenGL to use (Ex: #version 130)
     */
    public void initialize(String glslVersion) {
        ImGui.createContext();
        imguiGlfw = new ImGuiImplGlfw();
        imguiGlfw.init(windowManager.window, true);
        imguiGl3 = new ImGuiImplGl3();
        imguiGl3.init(glslVersion); // OpenGL version
    }

    /**
     * Starts a new ImGui frame.
     */
    public void newFrame() {
        imguiGlfw.newFrame();

        // Get the current window size dynamically
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowManager.window, width, height);

        // Set the display size for ImGui
        ImGui.getIO().setDisplaySize(width[0], height[0]);

        // Start a new ImGui frame
        ImGui.newFrame();
    }


    /**
     * Handles the keyboard and mouse input for IMGUI
     *
     * @param window the GLFW window handle.
     */
    public void handleInput(long window) {
        // Handle keyboard input
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            int state = GLFW.glfwGetKey(window, key);
            ImGui.getIO().setKeysDown(key, GLFW.GLFW_PRESS == state);
        }

        // Handle mouse input
        ImGui.getIO().setMouseDown(0, GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS);
        ImGui.getIO().setMouseDown(1, GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS);
        ImGui.getIO().setMouseDown(2, GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS);

        double[] mouseX = new double[1];
        double[] mouseY = new double[1];
        GLFW.glfwGetCursorPos(window, mouseX, mouseY);
        ImGui.getIO().setMousePos((float) mouseX[0], (float) mouseY[0]);

        // Handle scroll input
        // ImGui.getIO().setMouseWheel((float) GLFW.glfwGetScrollY(window));
    }

    /**
     * Renders the ImGui frame.
     */
    public void render() {
        ImGui.render();
        imguiGl3.renderDrawData(ImGui.getDrawData());
    }

    /**
     * Cleans up ImGui resources.
     */
    public void cleanup() {
        imguiGlfw.dispose();
        imguiGl3.dispose();
        ImGui.destroyContext();
    }
}
