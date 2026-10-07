package dev.shwg.shwgconfig.gui.components.tabs;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.components.widget.GenericTabNavigationBar;
import dev.shwg.shwgconfig.gui.layout.GenericLayout;
import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A single page of content behind {@link GenericTabNavigationBar}. Wraps a {@link GenericLayout}
 * rather than defining its own layout contract - sizing/arranging is delegated entirely to
 * whatever layout the implementor chooses (vertical stack, grid, a wrapped scrollable widget, etc.).
 */
public interface ITab {

    Component getTabTitle();

    default Component getTabExtraNarration() {
        return Component.empty();
    }

    GenericLayout getLayout();

    default void visitChildren(Consumer<LayoutElement> childrenConsumer) {
        getLayout().visitChildren(childrenConsumer);
    }

    default void visitWidgets(Consumer<GenericAbstractWidget> widgetConsumer){
        getLayout().visitWidgets(widgetConsumer);
    }

    /**
     * Called whenever this tab becomes current, and whenever the available content area
     * changes (e.g. screen resize). Hands the full area to the layout - width/height are
     * authoritative for layouts that use them as such (e.g. a wrapped scrollable widget),
     * and safely overwritten by {@code arrange()} for layouts that compute their own
     * main-axis size from children (vertical/horizontal stacks). Top-anchors the result.
     */
    default void adjustLayout(int x, int y, int width, int height) {
        GenericLayout layout = getLayout();
        layout.setWidth(width);
        layout.setHeight(height);
        layout.arrange();
        layout.setX(x);
        layout.setY(y);
    }
}
