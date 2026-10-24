package dev.shwg.shwgconfig.gui.components.controller.list;

import dev.shwg.shwgconfig.api.values.ListValue;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.layout.GenericHorizontalLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * One row of a list controller, showing one element of a {@link ListValue}.
 *
 * <p>You only build the middle part of the row. The list controller adds the
 * up/down buttons, the delete button and the "Add" button below the last row,
 * and saves the list whenever a row changes.</p>
 *
 * <p>A row does three things:</p>
 * <ul>
 *     <li>{@link #addWidget} - adds a widget. Widgets are placed side by side;
 *     the row is as wide and as tall as its widgets.</li>
 *     <li>{@link #commit} - call it when the row holds a complete, valid element.
 *     The list is saved right away.</li>
 *     <li>{@link #otherValues} - the elements of all <i>other</i> rows, e.g. to
 *     hide suggestions that are already used.</li>
 * </ul>
 *
 * <pre>{@code
 * public class NameRow extends ListRow<String> {
 *
 *     public NameRow(@Nullable String initial) {
 *         super(initial);
 *         GenericEditBox box = GenericEditBox.builder(font(), Component.empty())
 *                 .size(150, 20)
 *                 .responder(text -> {
 *                     if (!text.isBlank()) commit(text);
 *                 })
 *                 .build();
 *         if (initial != null) box.setValue(initial);
 *         addWidget(box);
 *     }
 * }
 *
 * category.addList(names, NameRow::new);
 * }</pre>
 *
 * <p>A row created with the "Add" button gets {@code null} as its initial element.
 * Until it calls {@link #commit} for the first time it counts as incomplete: it is
 * not saved, and the "Add" button stays disabled.</p>
 *
 * @param <E> the element type of the {@link ListValue} this row edits
 */
public abstract class ListRow<E> extends GenericHorizontalLayout {

    /** Space between the widgets of a row. */
    public static final int WIDGET_SPACING = 4;

    private @Nullable E value;
    private @Nullable ListRowOwner<E> owner;

    /**
     * @param initial the element this row starts with, or {@code null} for a new, empty row
     */
    protected ListRow(@Nullable E initial) {
        super(0, 0, 0, 0, WIDGET_SPACING, 0);
        this.value = initial;
    }

    /**
     * Adds a widget to the right of the previously added ones.
     */
    protected void addWidget(GenericAbstractWidget widget) {
        add(widget);
        setHeight(Math.max(getHeight(), widget.getHeight()));
        arrange(); // keeps getWidth() up to date
    }

    /**
     * Sets this row's element and saves the list. Call it whenever the row holds a
     * complete, valid element. Committing the element the row already has does nothing.
     *
     * <p>Safe to call from the constructor or before the row is shown - the element is
     * kept and picked up once the row is part of the list.</p>
     */
    protected final void commit(E newValue) {
        Objects.requireNonNull(newValue, "ListRow#commit needs a complete element, not null");
        if (newValue.equals(value)) return;
        value = newValue;
        if (owner != null) owner.rowChanged(this);
    }

    /**
     * The committed elements of all other rows, in list order. Incomplete rows are left
     * out. Read it when you need it (e.g. each time suggestions open) instead of
     * storing it, so it is always up to date.
     */
    protected final List<E> otherValues() {
        return owner == null ? List.of() : owner.valuesExcept(this);
    }

    /** The font to build widgets with. */
    protected final Font font() {
        return Minecraft.getInstance().font;
    }

    /** The last committed element, or {@code null} if this row was never complete. */
    public final @Nullable E getValue() {
        return value;
    }

    /** Whether this row has committed an element at least once. */
    public final boolean isComplete() {
        return value != null;
    }

    /** Called by the list controller when the row becomes part of the list. */
    final void attach(ListRowOwner<E> owner) {
        this.owner = owner;
    }
}
