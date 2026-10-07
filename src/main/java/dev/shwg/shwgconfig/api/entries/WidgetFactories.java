package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.values.ColorValue;
import dev.shwg.shwgconfig.api.values.stringvalues.StringValue;
import dev.shwg.shwgconfig.gui.components.widget.button.CycleButton;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericButton;
import dev.shwg.shwgconfig.gui.components.widget.dropdownwidget.DropdownWidget;
import dev.shwg.shwgconfig.gui.components.widget.editbox.GenericEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.HexColorEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.NumberEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;


public final class WidgetFactories {
    private WidgetFactories() {}

    // boolean toggle
    public static WidgetFactory<Boolean> toggle(Component trueText, Component falseText) {
        return (width, height, font, configValue) ->
                GenericButton.builder(configValue.getValue() ? trueText : falseText, btn -> {
                            boolean newValue = !configValue.getValue();
                            configValue.setValue(newValue);
                            btn.setMessage(newValue ? trueText : falseText);
                        })
                        .size(width, height)
                        .build();
    }

    // cycle button
    public static <T> WidgetFactory<T> cycle(List<T> options, Function<T, Component> labelFn) {
        return (width, height, font, configValue) ->
                CycleButton.of(options, labelFn, configValue.getValue())
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
    }

    // number editbox
    public static WidgetFactory<Integer> intInput(int min, int max, int step) {
        return (width, height, font, configValue) ->
                NumberEditBox.intEditBox(font, Component.empty())
                        .range(min, max)
                        .step(step)
                        .initialValue(configValue.getValue())
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
    }

    public static WidgetFactory<Float> floatInput(float min, float max, float step) {
        return (width, height, font, configValue) ->
                NumberEditBox.floatEditBox(font, Component.empty())
                        .range(min, max)
                        .step(step)
                        .initialValue(configValue.getValue())
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
    }

    public static WidgetFactory<Double> doubleInput(double min, double max, double step) {
        return (width, height, font, configValue) ->
                NumberEditBox.doubleEditBox(font, Component.empty())
                        .range(min, max)
                        .step(step)
                        .initialValue(configValue.getValue())
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
    }


    //editbox
    public static WidgetFactory<String> stringInput() {
        return (width, height, font, configValue) -> {
            GenericEditBox editBox = GenericEditBox.builder(font, Component.empty())
                    .responder(configValue::setValue)
                    .maxLength(64)
                    .size(width, height)
                    .build();
            editBox.setValue(configValue.getValue());
            return editBox;
        };
    }


    //color editbox
    public static WidgetFactory<Integer> color() {
        return (width, height, font, configValue) -> {
            ColorValue colorValue = (ColorValue) configValue;
            HexColorEditBox.Builder builder = colorValue.hasAlpha()
                    ? HexColorEditBox.argb(font, Component.empty())
                    : HexColorEditBox.rgb(font, Component.empty());
            return builder
                    .setColor(colorValue.getValue())
                    .applyValue(colorValue::setValue)
                    .size(width, height)
                    .build();
        };
    }

    //dropdown menu
    public static WidgetFactory<String> stringDropdown(List<String> options) {
        return (width, height, font, configValue) ->
                DropdownWidget.ofStrings(options)
                        .value(configValue.getValue())
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
    }

    public static <E extends Enum<E>> WidgetFactory<E> enumDropdown(Class<E> enumClass, Function<E, Component> labelFn) {
        return (width, height, font, configValue) ->
                DropdownWidget.ofEnum(enumClass, labelFn)
                        .value(configValue.getValue())
                        .applyValue(configValue::setValue)
                        .size(width, height)
                        .build();
    }


    //suggestion editbox
    public static WidgetFactory<String> suggestionBox(SuggestionEditBox.SuggestionProvider provider) {
        return (width, height, font, configValue) ->
        {
            String value = configValue.getValue();
            if (configValue instanceof StringValue stringValue) {
                value = stringValue.getDisplayName();
            }
            return SuggestionEditBox.builder(font, Component.empty(), provider)
                    .value(value)
                    .applyValue(configValue::setValue)
                    .size(width, height)
                    .overlayAlignment(Alignment.Horizontal.RIGHT)
                    .overlayWidth(150)
                    .build();
        };
    }



}
