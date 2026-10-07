package dev.shwg.shwgconfig.gui.deferred.overlay;

import dev.shwg.shwgconfig.gui.input.events.GenericCharacterEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.components.widget.OverlayWidget;
import dev.shwg.shwgconfig.gui.layout.GenericLayout;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

/**
 * "Overlays get input priority over normal widgets" logic shared by any container
 * (GenericScreen, GenericScrollableWidget, future ones). Each container calls this
 * against its own children() - since dispatch already recurses container by
 * container, this only ever needs to look one level deep to compose correctly
 * at any nesting depth.
 */
public final class OverlayDispatch {

    private OverlayDispatch() {}

    public static boolean mouseClicked(Iterable<? extends GuiEventListener> children, GenericMouseButtonEvent event) {
        for (GuiEventListener child : children) {
            if (child instanceof OverlayWidget widget && widget.isOverlayOpen() && widget.getOverlay().internalMouseClicked(event)) {
                return true;
            }
        }
        return false;
    }

    public static boolean mouseReleased(Iterable<? extends GuiEventListener> children, GenericMouseButtonEvent event) {
        for (GuiEventListener child : children) {
            if (child instanceof OverlayWidget widget && widget.isOverlayOpen() && widget.getOverlay().internalMouseReleased(event)) {
                return true;
            }
        }
        return false;
    }

    public static boolean mouseDragged(Iterable<? extends GuiEventListener> children, GenericMouseButtonEvent event, double deltaX, double deltaY) {
        for (GuiEventListener child : children) {
            if (child instanceof OverlayWidget widget && widget.isOverlayOpen() && widget.getOverlay().internalMouseDragged(event, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    public static boolean mouseScrolled(Iterable<? extends GuiEventListener> children, double mouseX, double mouseY, double horizontal, double vertical) {
        for (GuiEventListener child : children) {
            if (child instanceof OverlayWidget widget && widget.isOverlayOpen() && widget.getOverlay().internalMouseScrolled(mouseX, mouseY, horizontal, vertical)) {
                return true;
            }
        }
        return false;
    }

    public static boolean keyPressed(Iterable<? extends GuiEventListener> children, GenericKeyEvent event) {
        for (GuiEventListener child : children) {
            if (child instanceof OverlayWidget widget && widget.isOverlayOpen() && widget.getOverlay().internalKeyPressed(event)) {
                return true;
            }
        }
        return false;
    }

    public static boolean keyReleased(Iterable<? extends GuiEventListener> children, GenericKeyEvent event) {
        for (GuiEventListener child : children) {
            if (child instanceof OverlayWidget widget && widget.isOverlayOpen() && widget.getOverlay().internalKeyReleased(event)) {
                return true;
            }
        }
        return false;
    }

    public static boolean charTyped(Iterable<? extends GuiEventListener> children, GenericCharacterEvent event) {
        for (GuiEventListener child : children) {
            if (child instanceof OverlayWidget widget && widget.isOverlayOpen() && widget.getOverlay().internalCharTyped(event)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMouseBlocked(Iterable<? extends GuiEventListener> children, int mouseX, int mouseY) {
        for (GuiEventListener child : children) {
            if (child instanceof OverlayWidget widget) {
                WidgetOverlay overlay = widget.getOverlay();
                if (overlay.isOpen() && overlay.isHovered(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void closeOverlays(List<? extends GuiEventListener> listeners) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof OverlayWidget overlayWidget) {
                overlayWidget.getOverlay().open(false);
            } else if (listener instanceof GenericLayout layout) {
                layout.visitChildren( child -> {
                    if (child instanceof OverlayWidget overlayWidget) {
                        overlayWidget.getOverlay().open(false);
                    }
                });
            } else if (listener instanceof ContainerEventHandler eventHandler) {
                closeOverlays(eventHandler.children());
            }
        }
    }
}
