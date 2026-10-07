package dev.shwg.shwgconfig.gui.components.elements;

import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.render.GenericGraphics;

public class VerticalLineElement extends SpacerElement implements RenderableElement {

    private final int lineWidth;
    private int color;
    private boolean visible;

    public VerticalLineElement(int width, int height, int lineWidth, int color) {
        this(0, 0, width, height, lineWidth, color);
    }

    public VerticalLineElement(int x, int y, int width, int height, int lineWidth, int color) {
        super(x, y, width, height);
        this.lineWidth = lineWidth;
        this.visible = true;
        this.color = color;
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            int xCenter = this.getX() + this.getWidth() / 2;
            int x = xCenter - lineWidth / 2;
            graphics.fillWH(x, getY(), lineWidth, getHeight(), color);
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
