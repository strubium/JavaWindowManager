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

public class HtmlToImGui {

    // Persistent state
    private static final Map<String, ImBoolean> checkboxStates = new HashMap<>();
    private static final Map<String, ImFloat> sliderStates = new HashMap<>();
    private static final Map<String, ImInt> comboBoxStates = new HashMap<>();

    // Action handlers
    private static final Map<String, Runnable> buttonActions = new HashMap<>();
    private static final Map<String, Consumer<Boolean>> checkboxActions = new HashMap<>();
    private static final Map<String, Consumer<Float>> sliderActions = new HashMap<>();
    private static final Map<String, Consumer<Integer>> comboBoxActions = new HashMap<>();

    public static void renderHtml(GuiBuilder guiBuilder, String html) {
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        parseElement(guiBuilder, body, "");
    }

    // Register actions for all control types:
    public static void registerButtonAction(String id, Runnable action) {
        buttonActions.put(id, action);
    }

    public static void registerCheckboxAction(String id, Consumer<Boolean> action) {
        checkboxActions.put(id, action);
    }

    public static void registerSliderAction(String id, Consumer<Float> action) {
        sliderActions.put(id, action);
    }

    public static void registerComboBoxAction(String id, Consumer<Integer> action) {
        comboBoxActions.put(id, action);
    }

    // Print control IDs (unchanged)
    public static void printControlIds(String html) {
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        printIdsRecursive(body, "");
    }

    private static void printIdsRecursive(Element element, String path) {
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

    private static void parseElement(GuiBuilder guiBuilder, Element element, String path) {
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
                            // Trigger action if changed
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
                            // Trigger action if changed
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
                    // Trigger action if changed
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

    private static float parseFloatOrDefault(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
