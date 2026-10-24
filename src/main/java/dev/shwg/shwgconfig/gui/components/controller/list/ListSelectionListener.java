package dev.shwg.shwgconfig.gui.components.controller.list;

import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import org.jetbrains.annotations.Nullable;

/**
 * Told which row of a list is selected - the last row the user interacted with, or the
 * first row if none was yet. Typically implemented by a preview widget:
 *
 * <pre>{@code
 * MyPreview preview = new MyPreview();          // implements ListSelectionListener<String>
 * category.addList(names, NameRow::new, ListOptions.create().onSelectionChanged(preview));
 * category.preview(preview);
 * }</pre>
 *
 * @param <E> the element type of the list
 */
@FunctionalInterface
public interface ListSelectionListener<E> {

    /**
     * Called when the selected row or its element changes.
     *
     * @param value the selected row's committed element; {@code null} if the list is empty
     *              or the selected row is a new row that isn't complete yet
     * @param row   the selected row, or {@code null} if the list is empty. Read
     *              {@code row.getY()} when rendering instead of storing it - the row moves
     *              when scrolling or when rows are moved, added or removed.
     */
    void onSelectionChanged(@Nullable E value, @Nullable LayoutElement row);
}
