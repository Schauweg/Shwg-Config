package dev.shwg.shwgconfig.gui.layout;

import java.util.function.Consumer;

public class GenericFrameLayout extends GenericAbstractLayout {

    private final LayoutElement child;
    private final Alignment.Horizontal horizontalAlignment;
    private final Alignment.Vertical verticalAlignment;

    public GenericFrameLayout(int x, int y, int width, int height, LayoutElement child,
                              Alignment.Horizontal horizontalAlignment, Alignment.Vertical verticalAlignment) {
        super(x, y, width, height);
        this.child = child;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
    }

    @Override
    public void arrange() {
        if (child instanceof GenericLayout layout) {
            layout.arrange();
        }
        alignChild();
    }

    private void alignChild() {
        int childX = switch (horizontalAlignment) {
            case LEFT -> getX();
            case CENTER -> getX() + (getWidth() - child.getWidth()) / 2;
            case RIGHT -> getX() + getWidth() - child.getWidth();
        };
        int childY = switch (verticalAlignment) {
            case TOP -> getY();
            case CENTER -> getY() + (getHeight() - child.getHeight()) / 2;
            case BOTTOM -> getY() + getHeight() - child.getHeight();
        };
        child.setX(childX);
        child.setY(childY);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        consumer.accept(child);
    }

    @Override
    public int getContentWidth() {
        return getWidth();
    }

    @Override
    public int getContentHeight() {
        return getHeight();
    }
}
