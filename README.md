# JavaWindowManager

My library for creating GLFW windows with Imgui support

## Installing

Add [Jitpack](https://www.jitpack.io/#strubium/JavaWindowManager) to your build.gradle file
```
    maven { url 'https://jitpack.io' }
```

Add JavaWindowManager to your dependencies block

```
    implementation 'com.github.strubium:JavaWindowManager:1.0.0'
```

## Usage

### Components
#### WindowManager
Handles GLFW window creation, resizing, fullscreen toggling, and input callbacks.

#### ImguiHandler
Manages ImGui initialization, input polling, frame lifecycle, rendering, and cleanup.

#### GuiBuilder
Provides an abstraction layer to build ImGui widgets (buttons, sliders, checkboxes, combos).

#### HtmlToImGui
Parses HTML markup using Jsoup and converts elements into ImGui widgets via GuiBuilder. Supports action registration callbacks for user interaction.

### Usage Example 
Go look at `GuiHtmlTestApp` for a usage example as to how everything comes together