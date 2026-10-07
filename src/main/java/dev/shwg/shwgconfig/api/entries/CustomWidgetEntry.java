package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.api.values.ConfigValue;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;

/**
 * Wraps an already-built widget for direct placement in a {@link ConfigCategory},
 * bypassing the label/tooltip/enabled-state chrome {@link ControllerEntry} entries get.
 * Use this for a widget that doesn't map onto a single {@link ConfigValue} -
 * it's expected to manage its own tooltip and enabled/disabled state internally.
 */
public final class CustomWidgetEntry implements CategoryEntry {
    private final GenericAbstractWidget widget;

    public CustomWidgetEntry(GenericAbstractWidget widget) {
        this.widget = widget;
    }

    public GenericAbstractWidget getWidget() {
        return widget;
    }
}
