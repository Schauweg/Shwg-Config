package dev.shwg.shwgconfig.api.entries;

import dev.shwg.shwgconfig.api.CategoryBuilder;
import dev.shwg.shwgconfig.gui.components.controller.list.ListSelectionListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Optional settings for a list added with {@link CategoryBuilder#addList}.
 * Use {@link #NONE} (the default) for a list with none of these.
 *
 * <pre>{@code
 * ListOptions.create()
 *     .maxSize(10)
 *     .enabledWhen(featureEnabled::getValue)
 *     .onSelectionChanged(preview)
 * }</pre>
 */
public final class ListOptions {

    /** Shared instance for a list with no options set. */
    public static final ListOptions NONE = new ListOptions();

    private int maxSize = 0;
    private @Nullable Component addButtonText;
    private @Nullable Supplier<Boolean> enabledCondition;
    private @Nullable ListSelectionListener<?> selectionListener;

    /** Starts building a new set of options. */
    public static ListOptions create() {
        return new ListOptions();
    }

    /** The most rows the list may have; the "Add" button is disabled once it is full. 0 (default) = no limit. */
    public ListOptions maxSize(int maxSize) {
        this.maxSize = Math.max(0, maxSize);
        return this;
    }

    /** Text of the "Add" button below the last row. Defaults to a translated "Add". */
    public ListOptions addButtonText(Component text) {
        this.addButtonText = text;
        return this;
    }

    /**
     * Sets a condition controlling whether the list is enabled. Re-evaluated every frame;
     * while it returns {@code false}, rows can't be edited, moved, removed or added.
     */
    public ListOptions enabledWhen(Supplier<Boolean> condition) {
        this.enabledCondition = condition;
        return this;
    }

    /**
     * Gets told which row is selected, e.g. a preview widget that follows the selected row.
     * The element type must match the list's {@link dev.shwg.shwgconfig.api.values.ListValue}.
     */
    public ListOptions onSelectionChanged(ListSelectionListener<?> listener) {
        this.selectionListener = listener;
        return this;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public @Nullable Component getAddButtonText() {
        return addButtonText;
    }

    public @Nullable Supplier<Boolean> getEnabledCondition() {
        return enabledCondition;
    }

    public @Nullable ListSelectionListener<?> getSelectionListener() {
        return selectionListener;
    }
}
