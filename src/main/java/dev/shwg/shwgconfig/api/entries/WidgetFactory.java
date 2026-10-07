package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import dev.shwg.shwgconfig.api.values.ConfigValue;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import net.minecraft.client.gui.Font;

import java.util.function.Consumer;

/**
 * Builds the widget for a {@link ControllerEntry}. Given the space it has to work
 * with and the {@link ConfigValue} it's bound to, returns
 * a ready-to-render widget that reads its initial value from {@code configValue} and
 * writes changes back to it.
 *
 * <p>Most mod authors won't implement this directly - {@link CategoryBuilder}'s
 * built-in {@code addX(...)} methods already cover the common cases. Implement this
 * yourself only for a fully custom widget type used via the {@link CategoryBuilder}
 * escape hatch.
 *
 * @param <T> the value type this factory produces a widget for
 */
@FunctionalInterface
public interface WidgetFactory<T> {
    GenericAbstractWidget createWidget(int width, int height, Font font, ConfigValue<T> configValue);
}
