package dev.shwg.shwgconfig.api;

import dev.shwg.shwgconfig.api.entries.*;
import dev.shwg.shwgconfig.api.entries.display.EnumLabels;
import dev.shwg.shwgconfig.api.entries.display.NumberDisplay;
import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.api.values.numbervalues.DoubleValue;
import dev.shwg.shwgconfig.api.values.numbervalues.FloatValue;
import dev.shwg.shwgconfig.api.values.numbervalues.IntValue;
import dev.shwg.shwgconfig.api.values.stringvalues.*;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericKeyBindButton;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox;
import dev.shwg.shwgconfig.gui.components.widget.slider.NumberSlider;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import dev.shwg.shwgconfig.api.values.*;

/**
 * Fluent builder for a single {@link ConfigCategory} - a titled (or untitled, if
 * {@code title} is {@code null}) group of entries shown together on a config screen.
 * Each {@code addX(...)} method adds one entry bound to a {@link dev.shwg.shwgconfig.api.values.ConfigValue},
 * paired with the widget type the method name describes. Finish with {@link #build()}
 * and hand the result to {@link TabBuilder#addCategory(ConfigCategory)}.
 *
 * <pre>{@code
 * CategoryBuilder.create(Component.literal("General"))
 *     .addToggle(enableFeature, Component.literal("Enable Feature"))
 *     .addNumberSlider(renderDistance, Component.literal("Render Distance"))
 *     .build();
 * }</pre>
 */
public class CategoryBuilder {

    private final Component title;
    private final List<CategoryEntry> entries = new ArrayList<>();
    private RenderableElement previewWidget;

    private boolean centerPreviewWithControls = false;
    private boolean useOwnControllerWidth = false;

    private CategoryBuilder(Component title) {
        this.title = title;
    }

    /**
     * Starts building a category with the given title, shown above its entries.
     * Pass {@code null} for a category with no title.
     */
    public static CategoryBuilder create(Component title) {
        return new CategoryBuilder(title);
    }

    /**
     * Finishes building and returns the {@link ConfigCategory}.
     */
    public ConfigCategory build() {
        return new ConfigCategory(title, entries, previewWidget, centerPreviewWithControls, useOwnControllerWidth);
    }

    /**
     * Sets a preview widget shown beside this category's entries - e.g. a live
     * preview reacting to the entries above it. By default, it's positioned to the
     * right of the entry column, independent of that column's own centering; see
     * {@link #centerPreviewWithControls()} to center both as one combined block instead.
     */
    public CategoryBuilder preview(RenderableElement previewWidget) {
        this.previewWidget = previewWidget;
        return this;
    }

    /**
     * Centers this category's entry column and preview widget together as one
     * combined block, rather than centering just the entry column and placing the
     * preview beside it independently. Looks best when paired with {@link #useOwnControllerWidth()}
     * for a category whose preview is noticeably larger or smaller than its entries.
     */
    public CategoryBuilder centerPreviewWithControls() {
        this.centerPreviewWithControls = true;
        return this;
    }

    /**
     * Makes this category's entry column size itself to its own widest entry,
     * instead of sharing a uniform width with every other category in the same tab.
     * Useful for a category whose content (e.g. a custom widget) is much narrower
     * or wider than its siblings.
     */
    public CategoryBuilder useOwnControllerWidth() {
        this.useOwnControllerWidth = true;
        return this;
    }

    // --- Toggle Button
    /** @see #addToggle(BooleanValue, Component, Component, Component, EntryOptions) */
    public CategoryBuilder addToggle(BooleanValue value, Component label) {
        return addToggle(value, label, EntryOptions.NONE);
    }

    /** @see #addToggle(BooleanValue, Component, Component, Component, EntryOptions) */
    public CategoryBuilder addToggle(BooleanValue value, Component label, EntryOptions options) {
        return addToggle(value, label,
                Component.translatable("shwgconfig.button.on").withStyle(ChatFormatting.GREEN),
                Component.translatable("shwgconfig.button.off").withStyle(ChatFormatting.RED),
                options);
    }

    /** @see #addToggle(BooleanValue, Component, Component, Component, EntryOptions) */
    public CategoryBuilder addToggle(BooleanValue value, Component label, Component trueText, Component falseText) {
        return addToggle(value, label, trueText, falseText, EntryOptions.NONE);
    }

    /**
     * Adds a toggle button bound to {@code value}. Defaults to green "On"/red "Off" -
     * pass {@code trueText}/{@code falseText} for custom labels, e.g. <br>
     * {@code addToggle(hardMode, label, Component.literal("Hard"), Component.literal("Easy"))}.
     */
    public CategoryBuilder addToggle(BooleanValue value, Component label, Component trueText, Component falseText, EntryOptions options) {
        entries.add(new ControllerEntry<>(label, value, WidgetFactories.toggle(trueText, falseText), options));
        return this;
    }


    // --- Keybind Button
    /** @see #addKeybind(KeybindValue, EntryOptions) */
    public CategoryBuilder addKeybind(KeybindValue value) {
        return addKeybind(value, EntryOptions.NONE);
    }

    /**
     * Adds a keybind-capture button bound to {@code value}. The label is derived
     * automatically from the keybind's own translation key, mirroring vanilla's
     * Controls screen - unlike every other {@code addX} method, this one takes no
     * separate {@code label} parameter.
     */
    public CategoryBuilder addKeybind(KeybindValue value, EntryOptions options) {
        KeyMapping keyMapping = value.keyMapping();
        Component label = Component.translatable(keyMapping.getName());

        WidgetFactory<String> factory = (width, height, font, configValue) ->
                new GenericKeyBindButton.Builder(keyMapping.getTranslatedKeyMessage(), keyMapping)
                        .size(width, height)
                        .build();

        entries.add(new ControllerEntry<>(label, value, factory, options));
        return this;
    }


    // --- Number Slider
    /** @see #addNumberSlider(IntValue, Component, NumberDisplay, EntryOptions) */
    public CategoryBuilder addNumberSlider(IntValue value, Component label) {
        return addNumberSlider(value, label, NumberDisplay.raw(), EntryOptions.NONE);
    }

    /** @see #addNumberSlider(IntValue, Component, NumberDisplay, EntryOptions) */
    public CategoryBuilder addNumberSlider(IntValue value, Component label, NumberDisplay<Integer> display) {
        return addNumberSlider(value, label, display, EntryOptions.NONE);
    }

    /**
     * Adds a slider bound to {@code value}, using {@code value}'s own min/max/step.
     * {@code display} controls how the current value is formatted - see {@link NumberDisplay}
     * for raw/percentage/suffix/custom options; defaults to {@link NumberDisplay#raw()}.
     */
    public CategoryBuilder addNumberSlider(IntValue value, Component label, NumberDisplay<Integer> display, EntryOptions options) {
        WidgetFactory<Integer> factory = (width, height, font, configValue) ->
                NumberSlider.intSlider(Component.empty(), value.getMin(), value.getMax(), configValue.getValue(), value.getStep())
                        .valueToText(display.textFunction(value.getMin(), value.getMax()))
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
        entries.add(new ControllerEntry<>(label, value, factory, options));
        return this;
    }

    /** @see #addNumberSlider(FloatValue, Component, NumberDisplay, EntryOptions) */
    public CategoryBuilder addNumberSlider(FloatValue value, Component label) {
        return addNumberSlider(value, label, NumberDisplay.raw(), EntryOptions.NONE);
    }

    /** @see #addNumberSlider(FloatValue, Component, NumberDisplay, EntryOptions) */
    public CategoryBuilder addNumberSlider(FloatValue value, Component label, NumberDisplay<Float> display) {
        return addNumberSlider(value, label, display, EntryOptions.NONE);
    }

    /**
     * Adds a slider bound to {@code value}, using {@code value}'s own min/max/step.
     * {@code display} controls how the current value is formatted - see {@link NumberDisplay}
     * for raw/percentage/suffix/custom options; defaults to {@link NumberDisplay#raw()}.
     */
    public CategoryBuilder addNumberSlider(FloatValue value, Component label, NumberDisplay<Float> display, EntryOptions options) {
        WidgetFactory<Float> factory = (width, height, font, configValue) ->
                NumberSlider.floatSlider(Component.empty(), value.getMin(), value.getMax(), configValue.getValue(), value.getStep())
                        .valueToText(display.textFunction(value.getMin(), value.getMax()))
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
        entries.add(new ControllerEntry<>(label, value, factory, options));
        return this;
    }

    /** @see #addNumberSlider(DoubleValue, Component, NumberDisplay, EntryOptions) */
    public CategoryBuilder addNumberSlider(DoubleValue value, Component label) {
        return addNumberSlider(value, label, NumberDisplay.raw(), EntryOptions.NONE);
    }

    /** @see #addNumberSlider(DoubleValue, Component, NumberDisplay, EntryOptions) */
    public CategoryBuilder addNumberSlider(DoubleValue value, Component label, NumberDisplay<Double> display) {
        return addNumberSlider(value, label, display, EntryOptions.NONE);
    }

    /**
     * Adds a slider bound to {@code value}, using {@code value}'s own min/max/step.
     * {@code display} controls how the current value is formatted - see {@link NumberDisplay}
     * for raw/percentage/suffix/custom options; defaults to {@link NumberDisplay#raw()}.
     */
    public CategoryBuilder addNumberSlider(DoubleValue value, Component label, NumberDisplay<Double> display, EntryOptions options) {
        WidgetFactory<Double> factory = (width, height, font, configValue) ->
                NumberSlider.doubleSlider(Component.empty(), value.getMin(), value.getMax(), configValue.getValue(), value.getStep())
                        .valueToText(display.textFunction(value.getMin(), value.getMax()))
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
        entries.add(new ControllerEntry<>(label, value, factory, options));
        return this;
    }


    // --- Number EditBox
    /** @see #addNumberInput(IntValue, Component, EntryOptions) */
    public CategoryBuilder addNumberInput(IntValue value, Component label) {
        return addNumberInput(value, label, EntryOptions.NONE);
    }

    /**
     * Adds a typed number input field bound to {@code value}, constrained to its
     * own min/max/step. For a draggable alternative, see {@link #addNumberSlider}.
     */
    public CategoryBuilder addNumberInput(IntValue value, Component label, EntryOptions options) {
        entries.add(new ControllerEntry<>(label, value,
                WidgetFactories.intInput(value.getMin(), value.getMax(), value.getStep()), options));
        return this;
    }

    /** @see #addNumberInput(FloatValue, Component, EntryOptions) */
    public CategoryBuilder addNumberInput(FloatValue value, Component label) {
        return addNumberInput(value, label, EntryOptions.NONE);
    }

    /**
     * Adds a typed number input field bound to {@code value}, constrained to its
     * own min/max/step. For a draggable alternative, see {@link #addNumberSlider}.
     */
    public CategoryBuilder addNumberInput(FloatValue value, Component label, EntryOptions options) {
        entries.add(new ControllerEntry<>(label, value,
                WidgetFactories.floatInput(value.getMin(), value.getMax(), value.getStep()), options));
        return this;
    }

    /** @see #addNumberInput(DoubleValue, Component, EntryOptions) */
    public CategoryBuilder addNumberInput(DoubleValue value, Component label) {
        return addNumberInput(value, label, EntryOptions.NONE);
    }

    /**
     * Adds a typed number input field bound to {@code value}, constrained to its
     * own min/max/step. For a draggable alternative, see {@link #addNumberSlider}.
     */
    public CategoryBuilder addNumberInput(DoubleValue value, Component label, EntryOptions options) {
        entries.add(new ControllerEntry<>(label, value,
                WidgetFactories.doubleInput(value.getMin(), value.getMax(), value.getStep()), options));
        return this;
    }


    // --- Hex Color EditBox
    /** @see #addColor(ColorValue, Component, EntryOptions) */
    public CategoryBuilder addColor(ColorValue value, Component label) {
        return addColor(value, label, EntryOptions.NONE);
    }

    /**
     * Adds a hex color input bound to {@code value}, with a live swatch preview.
     * Shows an alpha channel if {@code value} was created via {@link ColorValue#argb(int)}.
     */
    public CategoryBuilder addColor(ColorValue value, Component label, EntryOptions options) {
        entries.add(new ControllerEntry<>(label, value, WidgetFactories.color(), options));
        return this;
    }


    // --- EditBox
    /** @see #addStringInput(StringValue, Component, EntryOptions) */
    public CategoryBuilder addStringInput(StringValue value, Component label) {
        return addStringInput(value, label, EntryOptions.NONE);
    }

    /**
     * Adds a plain text input field bound to {@code value}, updating on every
     * keystroke. For autocomplete suggestions, see {@link #addSuggestionInput}.
     */
    public CategoryBuilder addStringInput(StringValue value, Component label, EntryOptions options) {
        entries.add(new ControllerEntry<>(label, value, WidgetFactories.stringInput(), options));
        return this;
    }


    // --- Cycle Button
    /** @see #addCycleButton(StringValue, Component, List, EntryOptions) */
    public CategoryBuilder addCycleButton(StringValue value, Component label, List<String> options) {
        return addCycleButton(value, label, options, EntryOptions.NONE);
    }

    /**
     * Adds a button bound to {@code value} that cycles through {@code options} -
     * left-click advances, right-click goes back.
     */
    public CategoryBuilder addCycleButton(StringValue value, Component label, List<String> options, EntryOptions entryOptions) {
        entries.add(new ControllerEntry<>(label, value, WidgetFactories.cycle(options, Component::literal), entryOptions));
        return this;
    }

    /** @see #addCycleButton(EnumValue, Component, Function, EntryOptions) */
    public <E extends Enum<E>> CategoryBuilder addCycleButton(EnumValue<E> value, Component label) {
        return addCycleButton(value, label, EnumLabels.formatted(), EntryOptions.NONE);
    }

    /** @see #addCycleButton(EnumValue, Component, Function, EntryOptions) */
    public <E extends Enum<E>> CategoryBuilder addCycleButton(EnumValue<E> value, Component label, Function<E, Component> labelFn) {
        return addCycleButton(value, label, labelFn, EntryOptions.NONE);
    }

    /**
     * Adds a cycle button over every constant of {@code value}'s enum type. {@code labelFn}
     * controls how each constant is displayed - see {@link EnumLabels} for raw/formatted
     * options; defaults to {@link EnumLabels#formatted()} (e.g. {@code MY_VALUE} → "My Value").
     */
    public <E extends Enum<E>> CategoryBuilder addCycleButton(EnumValue<E> value, Component label, Function<E, Component> labelFn, EntryOptions options) {
        List<E> enumOptions = Arrays.asList(value.getEnumClass().getEnumConstants());
        entries.add(new ControllerEntry<>(label, value, WidgetFactories.cycle(enumOptions, labelFn), options));
        return this;
    }

    // --- Dropdown menu
    /** @see #addDropdown(StringValue, Component, List, EntryOptions) */
    public CategoryBuilder addDropdown(StringValue value, Component label, List<String> options) {
        return addDropdown(value, label, options, EntryOptions.NONE);
    }

    /**
     * Adds a dropdown bound to {@code value}, listing {@code options}.
     */
    public CategoryBuilder addDropdown(StringValue value, Component label, List<String> options, EntryOptions entryOptions) {
        entries.add(new ControllerEntry<>(label, value, WidgetFactories.stringDropdown(options), entryOptions));
        return this;
    }

    /** @see #addDropdown(EnumValue, Component, Function, EntryOptions) */
    public <E extends Enum<E>> CategoryBuilder addDropdown(EnumValue<E> value, Component label) {
        return addDropdown(value, label, EnumLabels.formatted(), EntryOptions.NONE);
    }

    /** @see #addDropdown(EnumValue, Component, Function, EntryOptions) */
    public <E extends Enum<E>> CategoryBuilder addDropdown(EnumValue<E> value, Component label, Function<E, Component> labelFn) {
        return addDropdown(value, label, labelFn, EntryOptions.NONE);
    }

    /**
     * Adds a dropdown listing every constant of {@code value}'s enum type. Same
     * {@code labelFn}/{@link EnumLabels} options as {@link #addCycleButton(EnumValue, Component, Function, EntryOptions)}.
     */
    public <E extends Enum<E>> CategoryBuilder addDropdown(EnumValue<E> value, Component label, Function<E, Component> labelFn, EntryOptions options) {
        entries.add(new ControllerEntry<>(label, value, WidgetFactories.enumDropdown(value.getEnumClass(), labelFn), options));
        return this;
    }

    // --- Suggestion EditBox
    /** @see #addSuggestionInput(StringValue, Component, SuggestionEditBox.SuggestionProvider, EntryOptions) */
    public CategoryBuilder addSuggestionInput(StringValue value, Component label, SuggestionEditBox.SuggestionProvider provider) {
        return addSuggestionInput(value, label, provider, EntryOptions.NONE);
    }

    /**
     * Adds a text field with autocomplete suggestions sourced from {@code provider}.
     * The general-purpose entry point for this widget - use {@link #addSuggestionInput(StringValue, Component, List, EntryOptions)}
     * for a fixed list, or {@link #addItemSuggestionInput}/{@link #addBlockSuggestionInput}
     * for registry search. Pass your own {@code provider} for anything else (e.g. a
     * dynamic or mod-specific source).
     */
    public CategoryBuilder addSuggestionInput(StringValue value, Component label, SuggestionEditBox.SuggestionProvider provider, EntryOptions options) {
        entries.add(new ControllerEntry<>(label, value, WidgetFactories.suggestionBox(provider), options));
        return this;
    }

    /** @see #addSuggestionInput(StringValue, Component, List, EntryOptions) */
    public CategoryBuilder addSuggestionInput(StringValue value, Component label, List<String> options) {
        return addSuggestionInput(value, label, options, EntryOptions.NONE);
    }

    /**
     * Adds a text field with autocomplete suggestions from a fixed {@code options} list.
     */
    public CategoryBuilder addSuggestionInput(StringValue value, Component label, List<String> options, EntryOptions entryOptions) {
        return addSuggestionInput(value, label, (query, overlay) -> SuggestionEditBox.getStringSuggestions(query, options, overlay), entryOptions);
    }

    /** @see #addItemSuggestionInput(ItemValue, Component, EntryOptions) */
    public CategoryBuilder addItemSuggestionInput(ItemValue value, Component label) {
        return addItemSuggestionInput(value, label, EntryOptions.NONE);
    }

    /**
     * Adds a text field with autocomplete suggestions from the item registry.
     */
    public CategoryBuilder addItemSuggestionInput(ItemValue value, Component label, EntryOptions options) {
        return addSuggestionInput(value, label, SuggestionEditBox::getItemSuggestions, options);
    }

    /** @see #addBlockSuggestionInput(BlockValue, Component, EntryOptions) */
    public CategoryBuilder addBlockSuggestionInput(BlockValue value, Component label) {
        return addBlockSuggestionInput(value, label, EntryOptions.NONE);
    }

    /**
     * Adds a text field with autocomplete suggestions from the block registry.
     */
    public CategoryBuilder addBlockSuggestionInput(BlockValue value, Component label, EntryOptions options) {
        return addSuggestionInput(value, label, SuggestionEditBox::getBlockSuggestions, options);
    }

    //--- Custom Widget
    /**
     * Drops a fully custom, pre-built widget into this category, bypassing the
     * label/tooltip/enabled-state chrome every other {@code addX} entry gets. The
     * widget is expected to manage its own value binding, tooltip, and enabled
     * state - see {@link CustomWidgetEntry}.
     */
    public CategoryBuilder addCustomWidget(GenericAbstractWidget widget) {
        entries.add(new CustomWidgetEntry(widget));
        return this;
    }

}
