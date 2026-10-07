package dev.shwg.shwgconfig.gui.components.widget;

import dev.shwg.shwgconfig.gui.deferred.overlay.WidgetOverlay;

public interface OverlayWidget {

    WidgetOverlay getOverlay();

    default boolean isOverlayOpen() {
        return getOverlay().isOpen();
    }
}
