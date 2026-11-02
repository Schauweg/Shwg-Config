package dev.shwg.shwgconfig.gui.deferred.overlay;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import dev.shwg.shwgconfig.render.GenericGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ListWidgetOverlay<E extends OverlayEntry> extends ScrollableWidgetOverlay {

    private final int maxVisibleEntries;
    private List<E> items = List.of();
    private int selectedIndex = -1;
    private final Alignment.HorizontalIconPosition iconPosition;

    protected ListWidgetOverlay(int width, Alignment.Horizontal alignment, Alignment.HorizontalIconPosition iconPosition, GenericAbstractWidget carrierWidget, int maxVisibleEntries) {
        super(width, 0, 0, alignment, carrierWidget);
        this.iconPosition = iconPosition;
        this.maxVisibleEntries = maxVisibleEntries;
    }

    public Alignment.HorizontalIconPosition getIconPosition() {
        return iconPosition;
    }

    /** Fires when the user picks a row - click, or Enter on the keyboard-selected one. */
    protected abstract void onEntryChosen(E entry);

    public List<E> getItems() { return items; }

    public boolean hasItems() { return !items.isEmpty(); }

    public void setItems(List<E> items) {
        this.items = items;
        selectedIndex = items.isEmpty() ? -1 : 0;
        if (selectedIndex >= 0) items.get(0).setSelected(true);
        int visibleHeight = 0;
        int limit = Math.min(items.size(), maxVisibleEntries);
        for (int i = 0; i < limit; i++) visibleHeight += items.get(i).getHeight();
        setHeight(visibleHeight);
    }

    public @Nullable E getSelected() {
        if (selectedIndex < 0 || selectedIndex >= items.size()) return null;
        return items.get(selectedIndex);
    }

    @Override
    protected int getContentHeight() {
        if (items == null) return 0;
        int total = 0;
        for (E entry : items) total += entry.getHeight();
        return total;
    }

    @Override
    protected void onScrollChanged() {
        if (items == null) return;
        int cumulative = 0;
        for (E entry : items) {
            entry.setY((int) (getY() + cumulative - getScrollOffset()));
            cumulative += entry.getHeight();
        }
    }

    @Override
    public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillWH(x, y, width, height, 0xC9000000);
        graphics.enableScissorWH(getX(), getY(), getWidth(), getHeight());
        for (E entry : items) {
            if (entry.getY() + entry.getHeight() >= getY() && entry.getY() <= getY() + getHeight()) {
                entry.render(graphics, mouseX, mouseY, partialTick);
            }
        }
        graphics.disableScissor();
        renderScrollbar(graphics, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(GenericMouseButtonEvent event) {
        if (!isOpen() || !isHovered(event)) return false;
        for (E entry : items) {
            if (entry.isHovered(event.getMouseX(), event.getMouseY())) {
                onEntryChosen(entry);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyPressed(GenericKeyEvent keyEvent) {
        if (((items.size() <= 1 && !keyEvent.isConfirmation())) || !isOpen()) {
            return false;
        }
        if (keyEvent.isDown() || (keyEvent.isTab() && !keyEvent.hasShiftDown())) { moveSelection(1); return true; }
        if (keyEvent.isUp() || (keyEvent.isTab() && keyEvent.hasShiftDown())) { moveSelection(-1); return true; }
        if (keyEvent.isConfirmation()) {
            if (selectedIndex < 0) return false;
            onEntryChosen(items.get(selectedIndex));
            return true;
        }
        return false;
    }

    /**
     * Highlights {@code entry} and scrolls so it sits in the middle of the list
     * (as far as the list can scroll). Meant for opening the overlay onto an
     * existing value. Does nothing if the entry isn't in this list.
     */
    public void setSelected(@Nullable E entry) {
        int index = entry != null ? items.indexOf(entry) : -1;
        if (index < 0) return;
        applySelection(index);

        int top = getEntryTop(index);
        int entryHeight = items.get(index).getHeight();
        setScrollOffset(top - (getHeight() - entryHeight) / 2.0);
    }

    private void moveSelection(int direction) {
        applySelection(Math.floorMod(selectedIndex + direction, items.size()));

        int top = getEntryTop(selectedIndex);
        int bottom = top + items.get(selectedIndex).getHeight();
        if (top < getScrollOffset()) setScrollOffset(top);
        else if (bottom > getScrollOffset() + getHeight()) setScrollOffset(bottom - getHeight());
    }

    /** Moves the highlight flag from the old row to the row at {@code index}. */
    private void applySelection(int index) {
        if (selectedIndex >= 0 && selectedIndex < items.size()) items.get(selectedIndex).setSelected(false);
        selectedIndex = index;
        items.get(selectedIndex).setSelected(true);
    }

    /** Distance from the top of the list content to the top of the row at {@code index}. */
    private int getEntryTop(int index) {
        int top = 0;
        for (int i = 0; i < index; i++) top += items.get(i).getHeight();
        return top;
    }
}
