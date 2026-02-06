package com.github.strubium.windowmanager.imgui;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.function.Consumer;

/**
 * Object-oriented utility class to allow you to use HTML files to make ImGui GUIs.
 * Each instance maintains its own state and actions.
 *
 * @author strubium
 */
public class HtmlToImGui {

    // Persistent state
    private final Map<String, ImBoolean> checkboxStates = new HashMap<>();
    private final Map<String, ImFloat> sliderStates = new HashMap<>();
    private final Map<String, ImInt> comboBoxStates = new HashMap<>();

    // Action handlers
    private final Map<String, Runnable> buttonActions = new HashMap<>();
    private final Map<String, Consumer<Boolean>> checkboxActions = new HashMap<>();
    private final Map<String, Consumer<Float>> sliderActions = new HashMap<>();
    private final Map<String, Consumer<Integer>> comboBoxActions = new HashMap<>();

    private String html = null;

    public HtmlToImGui(String html){
        this.html = html;
    }

    /**
     * Set the HTMl file to use (as a String)
     *
     * @param html The html file to use
     */
    public void setHtml(String html){
        this.html = html;
    }

    /**
     * Render your HTML -> ImGui UI
     *
     * @param guiBuilder The {@link GuiBuilder} class to use to make the window
     */
    public void renderHtml(GuiBuilder guiBuilder) {
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        parseElement(guiBuilder, body, "");
    }

    /**
     * Register an action for a button
     *
     * @param id The ID to use for this button, use {@link #printControlIds()} to all ids
     * @param action The action to run when the button is clicked
     */
    public void registerButtonAction(String id, Runnable action) {
        buttonActions.put(id, action);
    }

    /**
     * Register an action for a checkbox
     *
     * @param id The ID to use for this button, use {@link #printControlIds()} to all ids
     * @param action The action to run when the checkbox is toggled
     */
    public void registerCheckboxAction(String id, Consumer<Boolean> action) {
        checkboxActions.put(id, action);
    }

    /**
     * Register an action for a slider
     *
     * @param id The ID to use for this button, use {@link #printControlIds()} to all ids
     * @param action The action to run when the slider is slid
     */
    public void registerSliderAction(String id, Consumer<Float> action) {
        sliderActions.put(id, action);
    }

    /**
     * Register an action for a combo box (drop-down list)
     *
     * @param id The ID to use for this button, use {@link #printControlIds()} to all ids
     * @param action The action to run when the combo box is changed
     */
    public void registerComboBoxAction(String id, Consumer<Integer> action) {
        comboBoxActions.put(id, action);
    }

    /**
     * Print control IDs of the HTML file
     */
    public void printControlIds() {
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        printIdsRecursive(body, "");
    }

    private void printIdsRecursive(Element element, String path) {
        int index = 0;
        for (Element child : element.children()) {
            String id = path + "/" + child.tagName() + "[" + index + "]";
            index++;

            switch (child.tagName()) {
                case "button":
                    System.out.println("Button ID: " + id + " | Text: " + child.text());
                    break;
                case "input":
                    if ("range".equals(child.attr("type"))) {
                        System.out.println("Slider ID: " + id + " | Label: " + (child.hasAttr("label") ? child.attr("label") : "(no label)"));
                    } else if ("checkbox".equals(child.attr("type"))) {
                        System.out.println("Checkbox ID: " + id + " | Label: " + (child.hasAttr("label") ? child.attr("label") : "(no label)"));
                    }
                    break;
                case "select":
                    System.out.println("Dropdown ID: " + id + " | Label: " + (child.hasAttr("label") ? child.attr("label") : "(no label)"));
                    break;
            }

            printIdsRecursive(child, id);
        }
    }

    private void parseElement(GuiBuilder guiBuilder, Element element, String path) {
        int index = 0;
        for (Element child : element.children()) {
            String id = path + "/" + child.tagName() + "[" + index + "]";
            index++;

            switch (child.tagName()) {
                case "p":
                    guiBuilder.addText(child.text());
                    break;

                case "h1":
                case "h2":
                case "h3":
                    guiBuilder.pushFont("header");
                    guiBuilder.addTextCentered(child.text(), ImGui.getCursorPosY());
                    guiBuilder.popFont();
                    break;

                case "button":
                    final String buttonId = id;
                    guiBuilder.addButton(child.text(), () -> {
                        Runnable r = buttonActions.get(buttonId);
                        if (r != null) r.run();
                        else System.out.println("Button clicked (no action): " + child.text());
                    });
                    break;

                case "input":
                    String type = child.attr("type");
                    String label = child.hasAttr("label") ? child.attr("label") : id;
                    switch (type) {
                        case "checkbox":
                            ImBoolean checkboxVal = checkboxStates.computeIfAbsent(id, k -> new ImBoolean(false));
                            boolean oldCheckboxVal = checkboxVal.get();
                            guiBuilder.addCheckbox(label, checkboxVal);
                            if (checkboxVal.get() != oldCheckboxVal) {
                                Consumer<Boolean> action = checkboxActions.get(id);
                                if (action != null) action.accept(checkboxVal.get());
                            }
                            break;

                        case "range":
                            float min = parseFloatOrDefault(child.attr("min"), 0f);
                            float max = parseFloatOrDefault(child.attr("max"), 100f);
                            ImFloat sliderVal = sliderStates.computeIfAbsent(id, k -> new ImFloat(min));
                            float oldSliderVal = sliderVal.get();
                            guiBuilder.addSlider(label, sliderVal, min, max, "%.1f", 200);
                            if (sliderVal.get() != oldSliderVal) {
                                Consumer<Float> action = sliderActions.get(id);
                                if (action != null) action.accept(sliderVal.get());
                            }
                            break;

                        case "text":
                            guiBuilder.addText("[Text input not implemented]");
                            break;
                    }
                    break;

                case "select":
                    ImInt selected = comboBoxStates.computeIfAbsent(id, k -> new ImInt(0));
                    int oldSelected = selected.get();
                    List<String> options = new ArrayList<>();
                    for (Element option : child.select("option")) {
                        options.add(option.text());
                    }
                    guiBuilder.addComboBox(child.hasAttr("label") ? child.attr("label") : id, selected, options);
                    if (selected.get() != oldSelected) {
                        Consumer<Integer> action = comboBoxActions.get(id);
                        if (action != null) action.accept(selected.get());
                    }
                    break;

                case "div":
                case "span":
                    parseElement(guiBuilder, child, id);
                    break;

                default:
                    System.out.println("Unknown tag: " + child.tagName());
            }
        }
    }

    private float parseFloatOrDefault(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
