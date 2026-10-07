package dev.shwg.shwgconfig.gui.layout;

public abstract class GenericAbstractLayout implements GenericLayout {

    private int x, y, width, height;

    public GenericAbstractLayout(int x, int y, int width, int height) {
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
        this.visitChildren((layoutElement) -> {
            int newX = layoutElement.getX() + (x - this.getX());
            layoutElement.setX(newX);
        });
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.visitChildren(layoutElement -> {
            int newY = layoutElement.getY() + (y - this.getY());
            layoutElement.setY(newY);
        });
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
