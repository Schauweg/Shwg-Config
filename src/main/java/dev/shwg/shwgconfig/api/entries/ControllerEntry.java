package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import dev.shwg.shwgconfig.api.values.ConfigValue;
import net.minecraft.network.chat.Component;

/**
 * A declarative config entry: a label, a {@link ConfigValue}
 * to read/write, a {@link WidgetFactory} that knows how to build a widget for it, and
 * optional {@link EntryOptions} (tooltip, label tooltip, enabled condition).
 *
 * <p>You won't normally construct this directly - {@link CategoryBuilder}'s {@code addX(...)}
 * methods build one for you. Construct it yourself only when using the
 * {@link CategoryBuilder} escape hatch for a widget type the library doesn't provide a
 * named method for.
 *
 * @param <T> the value type this entry edits
 */
public class ControllerEntry<T> implements CategoryEntry {

    private final Component label;
    private final ConfigValue<T> configValue;
    private final WidgetFactory<T> widgetFactory;
    private final EntryOptions entryOptions;

    public ControllerEntry(Component label, ConfigValue<T> configValue, WidgetFactory<T> widgetFactory) {
        this(label, configValue, widgetFactory, EntryOptions.NONE);
    }

    public ControllerEntry(Component label, ConfigValue<T> configValue, WidgetFactory<T> widgetFactory, EntryOptions entryOptions) {
        this.label = label;
        this.configValue = configValue;
        this.widgetFactory = widgetFactory;
        this.entryOptions = entryOptions;
    }

    public Component getLabel() {
        return label;
    }

    public ConfigValue<T> getConfigValue() {
        return configValue;
    }

    public WidgetFactory<T> getWidgetFactory() {
        return widgetFactory;
    }

    public EntryOptions getEntryOptions() {
        return entryOptions;
    }
}
