package dev.shwg.shwgconfig.gui.layout;

public interface LayoutElement {

    int getX();
    int getY();
    int getWidth();
    int getHeight();

    void setX(int x);
    void setY(int y);
    void setWidth(int width);
    void setHeight(int height);

    default int getBottom() {
        return getY() + getHeight();
    }

    default int getRight() {
        return getX() + getWidth();
    }
}
