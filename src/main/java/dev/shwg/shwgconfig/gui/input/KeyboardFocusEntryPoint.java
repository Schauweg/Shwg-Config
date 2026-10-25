package dev.shwg.shwgconfig.gui.input;

import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by a container that decides itself which child gets focus when keyboard
 * navigation (arrow keys or Tab) enters it from outside - instead of the child nearest to
 * where the focus came from. Works on every version: used by vanilla's navigation through
 * {@code GenericAbstractContainerWidget#nextFocusPath} (1.19.4+) and by {@code SpatialNavigation} (older).
 *
 * <p>Example: a tab bar returns the button of the current tab. Without this, arrowing up
 * into the bar would focus (and so select) whichever tab happens to be closest.</p>
 */
public interface KeyboardFocusEntryPoint {

    /**
     * The child to focus when keyboard navigation enters this container, or {@code null} to
     * let the normal navigation pick one. May itself be a container; navigation then continues into it.
     */
    @Nullable GuiEventListener keyboardEntryFocus();
}
