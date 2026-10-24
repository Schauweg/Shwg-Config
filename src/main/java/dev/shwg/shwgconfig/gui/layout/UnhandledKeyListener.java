package dev.shwg.shwgconfig.gui.layout;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;

/**
 * Implemented by a {@link GenericLayout} that wants key presses its widgets didn't handle -
 * e.g. a list moving the focused row on Shift + arrow key.
 *
 * <p>When the focused widget doesn't use a key, the layouts containing that widget are asked,
 * innermost first, until one returns {@code true}. Layouts that don't contain the focused
 * widget are never asked.</p>
 */
public interface UnhandledKeyListener {

    /**
     * @param keyEvent the key the focused widget didn't handle
     * @param focused  the focused widget, somewhere inside this layout
     * @return {@code true} if the key was used
     */
    boolean onUnhandledKeyPressed(GenericKeyEvent keyEvent, GenericAbstractWidget focused);

    /**
     * Offers {@code keyEvent} to every layout below {@code root} (including {@code root}) that
     * contains {@code focused}, innermost first.
     *
     * @return {@code true} if one of them used the key
     */
    static boolean dispatch(LayoutElement root, GenericAbstractWidget focused, GenericKeyEvent keyEvent) {
        if (!(root instanceof GenericLayout layout) || !contains(layout, focused)) {
            return false;
        }
        boolean[] handled = {false};
        layout.visitChildren(child -> {
            if (!handled[0]) handled[0] = dispatch(child, focused, keyEvent);
        });
        if (handled[0]) {
            return true;
        }
        return layout instanceof UnhandledKeyListener listener && listener.onUnhandledKeyPressed(keyEvent, focused);
    }

    private static boolean contains(GenericLayout layout, GenericAbstractWidget widget) {
        boolean[] found = {false};
        layout.visitWidgets(w -> found[0] |= (w == widget));
        return found[0];
    }
}
