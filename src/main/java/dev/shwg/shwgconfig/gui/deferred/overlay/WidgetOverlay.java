package dev.shwg.shwgconfig.gui.deferred.overlay;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.deferred.DeferredElement;
import dev.shwg.shwgconfig.gui.input.events.GenericCharacterEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import org.jetbrains.annotations.NotNull;

public abstract class WidgetOverlay implements DeferredElement {

    public static int topBoundary, bottomBoundary;

    private final GenericAbstractWidget carrierWidget;

    protected int width, height;
    protected int x, y = 0;
    private int xOffset;
    protected boolean open;
    private Alignment.OverlayDirection direction;
    private final Alignment.Horizontal alignment;

    public WidgetOverlay(int width, int height, @NotNull Alignment.Horizontal alignment, @NotNull GenericAbstractWidget carrierWidget) {
        this.width = width;
        this.height = height;
        this.direction = getDefaultDirection();
        this.alignment = alignment;
        this.carrierWidget = carrierWidget;

        carrierWidget.setOnXChanged(X -> setX(X + xOffset));
        carrierWidget.setOnYChanged(Y -> updateDirection());
        carrierWidget.setOnWidthChanged(W -> updateXOffset());
        carrierWidget.setOnHeightChanged(H -> updateDirection());

        updateXOffset();
        updateDirection();
    }

    public Alignment.OverlayDirection getDirection() {
        return direction;
    }

    public abstract Alignment.OverlayDirection getDefaultDirection();

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
        updateDirection();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setWidth(int width) {
        this.width = width;
        updateXOffset();
    }

    public void setHeight(int height) {
        this.height = height;
        updateDirection();
    }

    public void dimensions(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getBounds() {
        return new int[]{x, y, x + width, y + height};
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    public boolean isHovered(GenericMouseButtonEvent event) {
        return isHovered(event.getMouseX(), event.getMouseY());
    }

    public boolean isOpen() {
        return open;
    }

    public void open(boolean open) {
        this.open = open;
    }

    protected void updateDirection() {
        if (carrierWidget.getY() - height < topBoundary) {
            this.direction = Alignment.OverlayDirection.BELOW;
        } else if (carrierWidget.getBottom() + height > bottomBoundary) {
            this.direction = Alignment.OverlayDirection.ABOVE;
        } else {
            this.direction = getDefaultDirection();
        }
        if (direction == Alignment.OverlayDirection.ABOVE) {
            y = carrierWidget.getY() - height;
        } else {
            y = carrierWidget.getBottom();
        }
    }

    protected void updateXOffset() {
        switch (alignment) {
            case LEFT -> xOffset = 0;
            case RIGHT -> xOffset = carrierWidget.getWidth() - width;
            case CENTER -> xOffset = carrierWidget.getWidth() / 2 - width / 2;
        }
        setX(carrierWidget.getX() + xOffset);
    }

    public boolean onKeyPressed(GenericKeyEvent keyEvent) {
        return false;
    }

    public boolean internalKeyPressed(GenericKeyEvent keyEvent) {
        return onKeyPressed(keyEvent);
    }

    public boolean onKeyReleased(GenericKeyEvent keyEvent) {
        return false;
    }

    public boolean internalKeyReleased(GenericKeyEvent keyEvent) {
        return onKeyReleased(keyEvent);
    }

    public boolean onCharTyped(GenericCharacterEvent charEvent) {
        return false;
    }

    public boolean internalCharTyped(GenericCharacterEvent charEvent) {
        return onCharTyped(charEvent);
    }

    public boolean onMouseClicked(GenericMouseButtonEvent mouseButtonEvent) {
        return false;
    }

    public boolean internalMouseClicked(GenericMouseButtonEvent mouseButtonEvent) {
        return onMouseClicked(mouseButtonEvent);
    }

    public boolean onMouseReleased(GenericMouseButtonEvent mouseButtonEvent) {
        return false;
    }

    public boolean internalMouseReleased(GenericMouseButtonEvent mouseButtonEvent) {
        return onMouseReleased(mouseButtonEvent);
    }

    public boolean onMouseDragged(GenericMouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        return false;
    }

    public boolean internalMouseDragged(GenericMouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        return onMouseDragged(mouseButtonEvent, deltaX, deltaY);
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        return false;
    }

    public boolean internalMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        return onMouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

}
