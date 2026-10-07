package dev.shwg.shwgconfig.gui.components.elements;

import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public class StringElement implements RenderableElement {

    protected Component component;
    protected final Font font;
    protected int x;
    protected int y;
    protected int color;
    protected boolean visible = true;

    public StringElement(Font font, String component, int x, int y, int color) {
        this(font, Component.literal(component), x,  y, color);
    }

    public StringElement(Font font, Component component, int x, int y, int color){
        this.component = component;
        this.font = font;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            graphics.drawString(font, component, x, y, color);

        }
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return font.width(component);
    }

    @Override
    public int getHeight() {
        return 8;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setWidth(int width) {

    }

    @Override
    public void setHeight(int height) {

    }
}
