package com.github.strubium.windowmanager;

import com.github.strubium.windowmanager.imgui.GuiBuilder;
import com.github.strubium.windowmanager.imgui.HtmlToImGui;
import com.github.strubium.windowmanager.imgui.ImguiHandler;
import com.github.strubium.windowmanager.window.WindowManager;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;

import static org.lwjgl.opengl.GL11.*;

public class GuiHtmlTestApp {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private static final WindowManager windowManager = new WindowManager(WIDTH, HEIGHT, false);
    private static ImguiHandler imguiHandler;

    static String html =
            "<h1>Welcome to ImGui</h1>" +
                    "<p>This UI is generated from HTML!</p>" +
                    "<button>Click Me</button>" +
                    "<input type=\"checkbox\" label=\"Enable Option\"/>" +
                    "<input type=\"range\" min=\"0\" max=\"100\" label=\"Volume\"/>" +
                    "<select label=\"Select Item\">" +
                    "<option>First</option>" +
                    "<option>Second</option>" +
                    "<option>Third</option>" +
                    "</select>";

    public static void main(String[] args) {
        HtmlToImGui.printControlIds(html);
        windowManager.createWindow("HTML to ImGui Test", true);
        windowManager.setupDefaultKeys();

        initImGui();

        GuiBuilder guiBuilder = new GuiBuilder();

        // Register actions once outside the loop
        HtmlToImGui.registerButtonAction("/button[2]", () -> System.out.println("Button was clicked!"));
        HtmlToImGui.registerCheckboxAction("/input[3]", checked -> System.out.println("Checkbox changed: " + checked));
        HtmlToImGui.registerSliderAction("/input[4]", value -> System.out.println("Slider changed: " + value));
        HtmlToImGui.registerComboBoxAction("/select[5]", selectedIndex -> System.out.println("Dropdown changed: " + selectedIndex));

        // Main loop
        while (!windowManager.shouldClose()) {
            imguiHandler.newFrame();

            // Optional: handle inputs if needed here
            imguiHandler.handleInput(windowManager.window);

            guiBuilder.beginWindow("Test Window");

            // Render your HTML -> ImGui UI
            HtmlToImGui.renderHtml(guiBuilder, html);

            guiBuilder.endWindow();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            imguiHandler.render();

            windowManager.swapAndPoll();
        }

        // Cleanup
        imguiHandler.cleanup();
        windowManager.destroy();
    }

    private static void initImGui() {
        imguiHandler = new ImguiHandler(windowManager);
        imguiHandler.initialize("#version 150");

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
    }
}
