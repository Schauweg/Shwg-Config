package dev.shwg.shwgconfig.gui.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class GenericVerticalLayout extends GenericAbstractLayout {

    private final int spacing, padding, scrollbarOffset;
    private final Alignment.Horizontal alignment;
    private final List<LayoutElement> children = new ArrayList<>();

    public GenericVerticalLayout(int x, int y, int width, int height, int spacing, int padding, Alignment.Horizontal alignment, int scrollbarOffset) {
        super(x, y, width, height);
        this.spacing = spacing;
        this.padding = padding;
        this.scrollbarOffset = scrollbarOffset;
        this.alignment = alignment;
    }

    public GenericVerticalLayout(int x, int y, int width, int height, int spacing, int padding, Alignment.Horizontal alignment){
        this(x, y, width, height, spacing, padding, alignment, 0);
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
        int currentY = getY() + padding;
        Iterator<LayoutElement> iterator = children.iterator();
        while (iterator.hasNext()) {
            LayoutElement element = iterator.next();
            int xPos = alignedX(element);
            element.setX(xPos);
            element.setY(currentY);

            if (element instanceof RenderableElement renderable) {
                currentY += renderable.getHeight();
            } else if (element instanceof GenericLayout nestedLayout) {
                nestedLayout.arrange();
                currentY += nestedLayout.getContentHeight();
            }
            if (iterator.hasNext()) {
                currentY += spacing;
            }
        }

        setHeight(currentY - getY() + padding);
    }

    private int alignedX(LayoutElement element) {
        switch (alignment) {
            case CENTER -> {
                return getX() + (getWidth() - element.getWidth()) / 2;
            }
            case RIGHT -> {
                return getX() + getWidth() - element.getWidth() - padding - scrollbarOffset;
            }
            case LEFT -> {
                return getX() + padding;
            }
        }
        return element.getX();
    }

    public int getContentHeight() {
        return getHeight();
    }

    @Override
    public int getContentWidth() {
        return getWidth();
    }

    public int getPadding() {
        return padding;
    }

    public int getSpacing() {
        return spacing;
    }

    public List<LayoutElement> getChildren() {
        return children;
    }
}
