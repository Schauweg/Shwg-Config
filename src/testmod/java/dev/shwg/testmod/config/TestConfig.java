package dev.shwg.testmod.config;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.api.*;
import dev.shwg.shwgconfig.api.entries.EntryOptions;
import dev.shwg.shwgconfig.api.entries.display.EnumLabels;
import dev.shwg.shwgconfig.api.entries.display.NumberDisplay;
import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.api.model.Tab;
import dev.shwg.shwgconfig.api.values.BooleanValue;
import dev.shwg.shwgconfig.api.values.ColorValue;
import dev.shwg.shwgconfig.api.values.EnumValue;
import dev.shwg.shwgconfig.api.values.numbervalues.DoubleValue;
import dev.shwg.shwgconfig.api.values.numbervalues.FloatValue;
import dev.shwg.shwgconfig.api.values.numbervalues.IntValue;
import dev.shwg.shwgconfig.api.values.stringvalues.*;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox;
import dev.shwg.shwgconfig.util.RegistryHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class TestConfig extends ConfigBase {

    private static final List<String> strings = List.of("String 1", "String 2", "String 3", "String 4", "String 5");
    private static final List<String> fakeSoundIds = List.of(
            "minecraft:entity.pig.ambient", "minecraft:block.anvil.use", "minecraft:ui.button.click");

    public enum TestEnum {ENUM1, ENUM2, ENUM3, ENUM4, ENUM5}

    // Tab 1: Basic controllers
    public final BooleanValue enableFeature = BooleanValue.of(true);
    public final BooleanValue dependentOption = BooleanValue.of(false);
    public final BooleanValue difficultyToggle = BooleanValue.of(true);

    public final IntValue intValue = IntValue.of(0, 0, 100);
    public final FloatValue floatValue = FloatValue.of(1.0f, 0f, 2f);
    public final DoubleValue doubleValue = DoubleValue.of(50.0, 0.0, 200.0);

    public final ColorValue argbColor = ColorValue.argb(0xAAFF000F);
    public final ColorValue rgbColor = ColorValue.rgb(0x00AAFF);
    public final StringValue stringInputValue = StringValue.of("Edit me");

    // Tab 2: Choice controllers
    public final StringValue cycleStringValue = StringValue.of(strings.get(0));
    public final EnumValue<TestEnum> cycleEnumValue = EnumValue.of(TestEnum.ENUM1);
    public final StringValue dropdownStringValue = StringValue.of(strings.get(0));
    public final EnumValue<TestEnum> dropdownEnumValue = EnumValue.of(TestEnum.ENUM1);

    // Tab 3: Suggestions & keybinds
    public final StringValue fixedListSuggestion = StringValue.of("");
    public final StringValue customProviderSuggestion = StringValue.of("");
    public final ItemValue itemValue = ItemValue.of(Items.DIAMOND);
    public final BlockValue blockValue = BlockValue.of(Blocks.DIRT);
    public final KeybindValue keybindValue = KeybindValue.ofKeyboard(
            "shwgconfig.keybind.test", RegistryHelper.build("shwgconfig", "test_category"), InputConstants.KEY_E);
    public final KeybindValue mouseKeybindValue = KeybindValue.ofMouse(
            "shwgconfig.keybind.mouse_test", RegistryHelper.build("shwgconfig", "test_category"), InputConstants.MOUSE_BUTTON_MIDDLE);

    // Tab 4: Layout & custom widgets
    public final BooleanValue previewToggleA = BooleanValue.of(true);
    public final BooleanValue previewToggleB = BooleanValue.of(false);
    public final BooleanValue noTitleToggle = BooleanValue.of(true);
    public final IntValue noTitleSlider = IntValue.of(5, 0, 10);
    public final CurveValue curveValue = CurveValue.empty();

    @Override
    public ConfigManager.ScreenManager buildScreen() {

        // Tab 1: Basic controllers
        ConfigCategory toggles = CategoryBuilder.create(Component.literal("Toggles"))
                .addToggle(enableFeature, Component.literal("Enable Feature"))
                .addToggle(dependentOption, Component.literal("Dependent Option"),
                        EntryOptions.create()
                                .enabledWhen(enableFeature::getValue)
                                .labelTooltip(Component.literal("Enabled when \"Enable Feature\" is on"))
                                .tooltip(Component.literal("Grayed out otherwise")))
                .addToggle(difficultyToggle, Component.literal("Difficulty"),
                        Component.literal("Hard").withStyle(ChatFormatting.RED),
                        Component.literal("Easy").withStyle(ChatFormatting.GREEN))
                .build();

        ConfigCategory numbers = CategoryBuilder.create(Component.literal("Numbers"))
                .addNumberSlider(intValue, Component.literal("Int Slider (percentage)"), NumberDisplay.percentage())
                .addNumberSlider(floatValue, Component.literal("Float Slider (suffix)"), NumberDisplay.suffix("x", true))
                .addNumberSlider(doubleValue, Component.literal("Double Slider (mapped %)"), NumberDisplay.mappedPercentage())
                .addNumberInput(intValue, Component.literal("Int Input (same value as above)"))
                .addNumberInput(floatValue, Component.literal("Float Input"))
                .addNumberInput(doubleValue, Component.literal("Double Input"))
                .build();

        ConfigCategory colorAndText = CategoryBuilder.create(Component.literal("Color & Text"))
                .addColor(argbColor, Component.literal("ARGB Color"))
                .addColor(rgbColor, Component.literal("RGB Color (no alpha)"))
                .addStringInput(stringInputValue, Component.literal("Plain Text Input"))
                .build();

        Tab basicControllers = TabBuilder.create(Component.literal("Basic Controllers"))
                .addCategory(toggles)
                .addCategory(numbers)
                .addCategory(colorAndText)
                .build();

        // Tab 2: Choice controllers
        ConfigCategory cycleButtons = CategoryBuilder.create(Component.literal("Cycle Buttons"))
                .addCycleButton(cycleStringValue, Component.literal("String Cycle"), strings)
                .addCycleButton(cycleEnumValue, Component.literal("Enum Cycle (formatted)"))
                .addCycleButton(cycleEnumValue, Component.literal("Enum Cycle (raw)"), EnumLabels.raw())
                .addCycleButton(cycleEnumValue, Component.literal("Enum Cycle (custom)"),
                        e -> Component.literal("#" + e.ordinal()).withStyle(ChatFormatting.AQUA))
                .build();

        ConfigCategory dropdowns = CategoryBuilder.create(Component.literal("Dropdowns"))
                .addDropdown(dropdownStringValue, Component.literal("String Dropdown"), strings)
                .addDropdown(dropdownEnumValue, Component.literal("Enum Dropdown (formatted)"))
                .addDropdown(dropdownEnumValue, Component.literal("Enum Dropdown (raw)"), EnumLabels.raw())
                .build();

        Tab choiceControllers = TabBuilder.create(Component.literal("Choice Controllers"))
                .addCategory(cycleButtons)
                .addCategory(dropdowns)
                .build();

        // Tab 3: Suggestions & keybinds
        ConfigCategory suggestions = CategoryBuilder.create(Component.literal("Suggestion Fields"))
                .addSuggestionInput(fixedListSuggestion, Component.literal("Fixed List"), strings)
                .addItemSuggestionInput(itemValue, Component.literal("Item Search"))
                .addBlockSuggestionInput(blockValue, Component.literal("Block Search"))
                .addSuggestionInput(customProviderSuggestion, Component.literal("Custom Provider"),
                        (query, overlay) -> SuggestionEditBox.getStringSuggestions(query, fakeSoundIds, overlay))
                .build();

        ConfigCategory keybinds = CategoryBuilder.create(Component.literal("Keybinds"))
                .addKeybind(keybindValue)
                .addKeybind(mouseKeybindValue)
                .build();

        Tab suggestionsAndKeybinds = TabBuilder.create(Component.literal("Suggestions & Keybinds"))
                .addCategory(suggestions)
                .addCategory(keybinds)
                .build();

        // Tab 4: Layout & custom widgets
        ConfigCategory defaultPreviewLayout = CategoryBuilder.create(Component.literal("Default Preview Layout"))
                .addToggle(previewToggleA, Component.literal("Toggle A"))
                .addToggle(previewToggleB, Component.literal("Toggle B"))
                .preview(InventoryWidget.builder(Component.literal("Inventory"))
                        .size(2, 3)
                        .addItem(Items.COBBLESTONE, 48, 0)
                        .addItem(Items.COBBLESTONE, 48, 5)
                        .build())
                .build();

        ConfigCategory noTitleCategory = CategoryBuilder.create(null)
                .addToggle(noTitleToggle, Component.literal("No Category Title"))
                .addNumberSlider(noTitleSlider, Component.literal("Slider"))
                .build();

        ConfigCategory customWidgetLayout = CategoryBuilder.create(Component.literal("Custom Widget, Own Width + Centered"))
                .addCustomWidget(new CatmullRomWidget(0, 0, 100, 100, 2, 4, 4, curveValue.getValue(), curveValue::setValue))
                .preview(InventoryWidget.builder(Component.literal("Inventory"))
                        .size(4, 3)
                        .addItem(Items.COBBLESTONE, 64, 0)
                        .addItem(Items.DIAMOND, 64, 1)
                        .addItem(Items.DIAMOND, 64, 2)
                        .addItem(Items.STICK, 32, 9)
                        .addItem(Items.STICK, 64, 10)
                        .addItem(Items.STICK, 64, 11)
                        .build())
                .useOwnControllerWidth()
                .centerPreviewWithControls()
                .build();

        Tab layoutAndCustomWidgets = TabBuilder.create(Component.literal("Layout & Custom Widgets"))
                .addCategory(defaultPreviewLayout)
                .addCategory(noTitleCategory)
                .addCategory(customWidgetLayout)
                .build();

        return Builder.create(Component.literal("Test Mod Config"))
                .addTab(basicControllers)
                .addTab(choiceControllers)
                .addTab(suggestionsAndKeybinds)
                .addTab(layoutAndCustomWidgets)
                .build();
    }
}
