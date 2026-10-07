package dev.shwg.shwgconfig.gui.layout;

import dev.shwg.shwgconfig.render.GenericGraphics;

public interface RenderableElement extends LayoutElement {

    void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks);

    default boolean isVisible() {
        return true;
    }

    void setVisible(boolean visible);
}
