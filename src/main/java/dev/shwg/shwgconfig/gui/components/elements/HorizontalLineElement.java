package dev.shwg.shwgconfig.gui.components.elements;

import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.render.GenericGraphics;

public class HorizontalLineElement extends SpacerElement implements RenderableElement {

    private final int lineHeight;
    private int color;
    private boolean visible;

    public HorizontalLineElement(int width, int height, int lineHeight, int color) {
        this(0, 0, width, height, lineHeight, color);
    }

    public HorizontalLineElement(int x, int y, int width, int height, int lineHeight, int color) {
        super(x, y, width, height);
        this.lineHeight = lineHeight;
        this.visible = true;
        this.color = color;
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            int yCenter = this.getY() + this.getHeight() / 2;
            int y = yCenter - this.lineHeight / 2;
            graphics.fillWH(getX(), y, getWidth(), lineHeight, color);
        }
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
