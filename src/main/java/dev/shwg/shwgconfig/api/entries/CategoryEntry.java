package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.api.values.ConfigValue;

/**
 * Marker interface for anything that can be added to a {@link ConfigCategory} -
 * either a {@link ControllerEntry} (a {@link ConfigValue}
 * bound to a widget) or a {@link CustomWidgetEntry} (a pre-built widget dropped in as-is).
 * You won't normally implement this directly; use {@link CategoryBuilder} instead.
 */
public interface CategoryEntry {
}
