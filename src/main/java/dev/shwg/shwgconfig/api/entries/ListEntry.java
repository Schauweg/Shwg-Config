package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import dev.shwg.shwgconfig.api.values.ListValue;
import dev.shwg.shwgconfig.gui.components.controller.list.ListRow;

import java.util.function.Function;

/**
 * A list entry: a {@link ListValue}, the factory that builds one {@link ListRow} per element,
 * and its {@link ListOptions}. Built for you by {@link CategoryBuilder#addList}.
 *
 * @param <E> the element type of the list
 */
public final class ListEntry<E> implements CategoryEntry {

    private final ListValue<E> value;
    private final Function<E, ListRow<E>> rowFactory;
    private final ListOptions options;

    public ListEntry(ListValue<E> value, Function<E, ListRow<E>> rowFactory, ListOptions options) {
        this.value = value;
        this.rowFactory = rowFactory;
        this.options = options;
    }

    public ListValue<E> getValue() {
        return value;
    }

    /** Builds a row for {@code element}, or an empty row for {@code null}. */
    public Function<E, ListRow<E>> getRowFactory() {
        return rowFactory;
    }

    public ListOptions getOptions() {
        return options;
    }
}
