package dev.shwg.shwgconfig.gui.layout;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.render.GenericGraphics;

import java.util.function.Consumer;

public interface GenericLayout extends LayoutElement {

    void visitChildren(Consumer<LayoutElement> consumer);

    default void visitWidgets(Consumer<GenericAbstractWidget> consumer) {
        visitChildren(element -> {
            if (element instanceof GenericAbstractWidget widget) {
                consumer.accept(widget);
            } else if (element instanceof GenericLayout nestedLayout) {
                nestedLayout.visitWidgets(consumer);
            }
        });
    }

    default void arrange() {
        visitChildren(element -> {
            if (element instanceof GenericLayout layout) {
                layout.arrange();
            }
        });
    }

    default void renderChildren(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        visitChildren(child -> renderChild(child, graphics, mouseX, mouseY, partialTicks));
    }

    private static void renderChild(LayoutElement element, GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (element instanceof RenderableElement renderable) {
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        } else if (element instanceof GenericLayout nested) {
            nested.renderChildren(graphics, mouseX, mouseY, partialTicks);
        }
    }

    int getContentHeight();

    int getContentWidth();
}
