package com.github.strubium.windowmanager.imgui;

import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A builder class for constructing GUI elements using ImGui.
 */
public class GuiBuilder {
    private static final Map<String, ImFont> fonts = new HashMap<>();
    private static ImFont currentFont;

    /**
     * Sets a font for ImGui using a specified alias.
     *
     * @param alias    The alias for the font.
     * @param fontPath The file path of the font.
     * @param fontSize The size of the font.
     */
    public static void setFont(String alias, String fontPath, float fontSize) {
        ImGuiIO io = ImGui.getIO();
        ImFontAtlas fontAtlas = io.getFonts();
        ImFont font = fontAtlas.addFontFromFileTTF(fontPath, fontSize);
        if (font != null) {
            fonts.put(alias, font);
        }
    }

    /**
     * Begins a new window with the specified name.
     *
     * @param name The title of the window.
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder beginWindow(String name) {
        return beginWindow(name, 0); // Default flags as 0
    }

    /**
     * Begins a new window with the specified name and flags.
     *
     * @param name  The title of the window.
     * @param flags The window flags.
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder beginWindow(String name, int flags) {
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
        ImGui.begin(name, flags);
        return this;
    }

    /**
     * Ends the current window.
     *
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder endWindow() {
        ImGui.end();
        return this;
    }

    /**
     * Sets the cursor position within the window.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder setPos(float x, float y){
        ImGui.setCursorPos(x, y);
        return this;
    }

    /**
     * Pushes a font onto the ImGui stack.
     *
     * @param alias The alias of the font.
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder pushFont(String alias) {
        ImFont font = fonts.get(alias);
        if (font != null) {
            ImGui.pushFont(font);
            currentFont = font;
        }
        return this;
    }

    /**
     * Pops the last pushed font from the ImGui stack.
     *
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder popFont() {
        if (currentFont != null) {
            ImGui.popFont();
            currentFont = null;
        }
        return this;
    }

    /**
     * Renders a top toolbar menu with submenus and actions.
     *
     * @param menuLabels    List of main menu labels.
     * @param subMenuLabels List of lists containing sub-menu labels.
     * @param subMenuActions List of lists containing actions for each submenu item.
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder topToolbar(List<String> menuLabels, List<List<String>> subMenuLabels, List<List<Runnable>> subMenuActions) {
        // Ensure all lists have the correct size
        if (menuLabels.size() != subMenuLabels.size() || menuLabels.size() != subMenuActions.size()) {
            throw new IllegalArgumentException("Menu labels, sub-menu labels, and sub-menu actions must have the same size.");
        }

        // Begin the main menu bar
        if (ImGui.beginMainMenuBar()) {
            for (int i = 0; i < menuLabels.size(); i++) {
                if (ImGui.beginMenu(menuLabels.get(i))) {
                    // Render each sub-menu item
                    List<String> subMenu = subMenuLabels.get(i);
                    List<Runnable> actions = subMenuActions.get(i);

                    for (int j = 0; j < subMenu.size(); j++) {
                        if (ImGui.menuItem(subMenu.get(j))) {
                            // Run the associated action when the sub-menu item is clicked
                            actions.get(j).run();
                        }
                    }
                    ImGui.endMenu();
                }
            }
            ImGui.endMainMenuBar(); // End the main menu bar
        }

        return this;
    }


    /**
     * Add a button to the GUI
     *
     * @param label  The label for this button.
     * @param onClick What to do when this button is clicked.
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder addButton(String label, Runnable onClick) {
        if (ImGui.button(label)) {
            onClick.run();
        }
        return this;
    }

    /**
     * Add some text to the GUI
     *
     * @param text  The text to add.
     * @return The current GuiBuilder instance.
     */
    public GuiBuilder addText(String text) {
        ImGui.text(text);
        return this;
    }

    public GuiBuilder addCheckbox(String label, ImBoolean value) {
        ImGui.checkbox(label, value);
        return this;
    }

    public GuiBuilder addComboBox(String label, ImInt selectedIndex, List<String> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options for combo box cannot be null or empty.");
        }

        String[] optionsArray = options.toArray(new String[0]);

        if (ImGui.beginCombo(label, optionsArray[selectedIndex.get()])) {
            for (int i = 0; i < optionsArray.length; i++) {
                boolean selected = (i == selectedIndex.get());
                if (ImGui.selectable(optionsArray[i], selected)) {
                    selectedIndex.set(i);
                }
            }
            ImGui.endCombo();
        }

        return this;
    }
    public GuiBuilder addSlider(String label, ImFloat value, float minValue, float maxValue, String format, float maxWidth) {
        float screenWidth = ImGui.getIO().getDisplaySizeX();
        ImGui.setCursorPos((screenWidth - maxWidth) / 2, ImGui.getCursorPosY());
        ImGui.pushItemWidth(maxWidth); // Set max width for the slider
        if (ImGui.sliderFloat(label, value.getData(), minValue, maxValue, format)) {
            // Handle value change if needed
        }
        ImGui.popItemWidth(); // Reset item width after
        return this;
    }

    // New Method: addFloatInput for float values
    public GuiBuilder addFloatInput(String label, ImFloat value, float maxWidth) {
        float screenWidth = ImGui.getIO().getDisplaySizeX();
        ImGui.setCursorPos((screenWidth - maxWidth) / 2, ImGui.getCursorPosY());
        ImGui.pushItemWidth(maxWidth); // Set max width for the input field
        if (ImGui.inputFloat(label, value)) {
            // Handle value change if needed
        }
        ImGui.popItemWidth(); // Reset item width after
        return this;
    }

    public GuiBuilder addTextCentered(String text, float yOffset) {
        float screenWidth = ImGui.getIO().getDisplaySizeX();
        float textWidth = ImGui.calcTextSize(text).x;
        ImGui.setCursorPos((screenWidth - textWidth) / 2, yOffset);
        ImGui.text(text);
        return this;
    }

    public GuiBuilder addButtonCentered(String label, Runnable onClick, float yOffset, float paddingWidth, float paddingHeight) {
        float screenWidth = ImGui.getIO().getDisplaySizeX();
        float textWidth = ImGui.calcTextSize(label).x;
        float buttonWidth = textWidth + paddingWidth; // Ensure padding allows for a nice button size

        ImGui.setCursorPos((screenWidth - buttonWidth) / 2, yOffset);
        if (ImGui.button(label, buttonWidth, 30 + paddingHeight)) {
            onClick.run();
        }
        return this;
    }


    public GuiBuilder addTextAtPosition(String text, float x, float y) {
        ImGui.setCursorPos(x, y);
        ImGui.text(text);
        return this;
    }

    /**
     * Renders the ImGui frame.
     */
    public void render() {
        ImGui.render();
    }
}