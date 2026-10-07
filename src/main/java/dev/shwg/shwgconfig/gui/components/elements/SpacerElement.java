package dev.shwg.shwgconfig.gui.components.elements;

import dev.shwg.shwgconfig.gui.layout.LayoutElement;

public class SpacerElement implements LayoutElement {

    private int x;
    private int y;
    private int width;
    private int height;

    public SpacerElement(int width, int height) {
        this(0, 0, width, height);
    }

    public SpacerElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
        return width;
    }

    @Override
    public int getHeight() {
        return height;
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
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }
}
