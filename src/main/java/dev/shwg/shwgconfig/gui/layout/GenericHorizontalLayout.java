package dev.shwg.shwgconfig.gui.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class GenericHorizontalLayout extends GenericAbstractLayout {

    public final int spacing, padding;
    private final Alignment.Vertical alignment;
    private final List<LayoutElement> children = new ArrayList<>();

    public GenericHorizontalLayout(int x, int y, int width, int height, int spacing, int padding, Alignment.Vertical alignment) {
        super(x, y, width, height);
        this.spacing = spacing;
        this.padding = padding;
        this.alignment = alignment;
    }

    public GenericHorizontalLayout(int x, int y, int width, int height, int spacing, int padding) {
        this(x, y, width, height, spacing, padding, Alignment.Vertical.CENTER);
    }

    public void add(LayoutElement child) {
        children.add(child);
    }

    public void remove(LayoutElement child) {
        children.remove(child);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        for (LayoutElement child : children) {
            consumer.accept(child);
        }
    }

    @Override
    public void arrange() {
        int currentX = getX() + padding;
        Iterator<LayoutElement> iterator = children.iterator();

        while (iterator.hasNext()) {
            LayoutElement element = iterator.next();
            int yPos = alignedY(element);

            element.setX(currentX);
            element.setY(yPos);

            if (element instanceof RenderableElement renderable) {
                currentX += renderable.getWidth();
            } else if (element instanceof GenericLayout nestedLayout) {
                nestedLayout.arrange();
                currentX += nestedLayout.getContentWidth();
            }

            if (iterator.hasNext()) {
                currentX += spacing;
            }
        }

        setWidth(currentX - getX() + padding);
    }

    private int alignedY(LayoutElement element) {
        switch (alignment) {
            case CENTER -> {
                return getY() + (getHeight() - element.getHeight()) / 2;
            }
            case BOTTOM -> {
                return getY() + getHeight() - element.getHeight() - padding;
            }
            case TOP -> {
                return getY() + padding;
            }
        }
        return element.getY();
    }

    @Override
    public int getContentWidth() {
        return getWidth();
    }

    @Override
    public int getContentHeight() {
        return getHeight();
    }

    public List<LayoutElement> getChildren() {
        return children;
    }
}
