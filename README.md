# ShwgConfig

A configuration and GUI library for Minecraft mods. Define your config as plain fields, describe the screen with a small builder API, and get a resizable, multi-tab config screen without positioning widgets by hand.

It's built with [Stonecraft](https://stonecraft.meza.gg/) (Stonecutter + Architectury), so it targets a wide range of Minecraft versions and mod loaders (Fabric, NeoForge, Forge) and handles the GUI API differences between them for you.

> **Note:** ShwgConfig is still a work in progress. The core is usable, but expect some rough edges and API changes before 1.0.

## Features

- **Declarative screen building** - describe tabs, categories, and entries with a small fluent builder, get a fully laid-out screen back
- **A bunch of built-in controllers** - toggles, sliders (with raw/percentage/suffix/custom display formatting), number input fields, dropdowns, cycle buttons, string/item/block search fields, keybinds, and a color picker
- **Custom widgets** - drop your own widget straight into a category if none of the built-ins fit
- **Tooltips and conditional enabling** - per-entry tooltips (separately for the label and the control), and `enabledWhen(...)` to gray out entries based on other config values
- **Automatic JSON persistence** - config values save/load themselves, no manual serialization code
- **Multi-loader, multi-version** - built with Stonecraft (Stonecutter + Architectury) so the same mod source can target Fabric, NeoForge, and Forge across a range of MC versions

<details>
  <summary>Screenshot</summary>
<img src="https://cdn.modrinth.com/data/pWM8kTBg/images/efe937a7bf1bc25a5fe4ececac253991829547d3.png" alt="screenshot of basic controllers the library provides">
</details>

## Quick example

```java
public class MyConfig extends ConfigBase {

    public final BooleanValue enableFeature = BooleanValue.of(true);
    public final IntValue renderDistance = IntValue.of(8, 2, 32);
    public final ColorValue accentColor = ColorValue.argb(0xFFAA00FF);

    @Override
    public ConfigManager.ScreenManager buildScreen() {
        ConfigCategory general = CategoryBuilder.create(Component.literal("General"))
            .addToggle(enableFeature, Component.literal("Enable Feature"))
            .addNumberSlider(renderDistance, Component.literal("Render Distance"))
            .addColor(accentColor, Component.literal("Accent Color"))
            .build();

        return Builder.create(Component.literal("My Mod"))
            .addSingleTabCategory(general)
            .build();
    }
}
```

```java
public static final MyConfig CONFIG = new MyConfig();
public static final ConfigManager MANAGER = ConfigManager.of(CONFIG, "mymod.json");

public void onInitializeClient() {
    MANAGER.load();
}
```

That's enough to get a working, saved, resizable config screen. Open it with `MANAGER.getScreen(parentScreen)`.

For a more complete example you can take a look at the [Test Mod](https://github.com/Schauweg/Shwg-Config/blob/main/src/testmod/java/dev/shwg/testmod/TestMod.java) inside this repository.

## Installation

Published on [Modrinth](https://modrinth.com/mod/shwgconfig) (also available on the Modrinth Maven) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/shwgconfig). Add it as a dependency through whichever of those you already use for your mod's other dependencies.

## Available controllers and value types

| Controller | Value type(s) |
|---|---|
| Toggle | `BooleanValue` |
| Slider | `IntValue`, `FloatValue`, `DoubleValue` |
| Number input | `IntValue`, `FloatValue`, `DoubleValue` |
| Dropdown | `StringValue`, `EnumValue<E>` |
| Cycle button | `StringValue`, `EnumValue<E>` |
| Suggestion field | `StringValue`, `ItemValue`, `BlockValue` |
| Keybind | `KeybindValue` |
| Color picker | `ColorValue` |

Plus an escape hatch (`addEntry(...)`) for anything not covered above, and `addCustomWidget(...)` for dropping in a fully custom widget.

## License

MIT
