package dev.shwg.shwgconfig.gui.components.widget.dropdownwidget;

import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import dev.shwg.shwgconfig.gui.deferred.overlay.OverlayEntry;
import dev.shwg.shwgconfig.gui.deferred.overlay.ScrollableWidgetOverlay;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public abstract class DropdownEntry<T> implements OverlayEntry {

    private static final int ICON_SIZE = 10;
    private static final int ICON_PADDING = 2;

    private final ListWidgetOverlay<?> owner; // was: ListWidgetOverlay<DropdownEntry<?>>
    private int y;
    private final int height;
    private boolean selected;

    protected DropdownEntry(ListWidgetOverlay<?> owner, int height) { // was: ListWidgetOverlay<DropdownEntry<?>>
        this.owner = owner;
        this.height = height;
    }


    public abstract T value();
    public abstract Component displayComponent();

    protected boolean hasIcon() {
        return false;
    }

    protected void renderIcon(GenericGraphics graphics, int x, int y, int size, float partialTick) {
    }

    public final boolean hasVisibleIcon() {
        return hasIcon();
    }

    public final void drawIcon(GenericGraphics graphics, int x, int y, int size, float partialTick) {
        if (hasIcon()) {
            renderIcon(graphics, x, y, size, partialTick);
        }
    }

    protected final int getContentWidth() {
        int reserved = owner.getScrollbarReservedWidth();
        if (hasIcon()) {
            reserved += ICON_SIZE + ICON_PADDING * 2;
        }
        return getWidth() - reserved;
    }

    protected final int getContentX() {
        if (hasIcon() && owner.getIconPosition() == Alignment.HorizontalIconPosition.LEFT) {
            return getIconX() + ICON_SIZE + ICON_PADDING;
        }
        return getX() + ICON_PADDING;
    }

    private int getIconX() {
        if (owner.getIconPosition() == Alignment.HorizontalIconPosition.LEFT) {
            return getX() + ICON_PADDING;
        }
        return getX() + getWidth() - owner.getScrollbarReservedWidth() - ICON_SIZE;
    }

    private int getIconY() {
        return getY() + (getHeight() - ICON_SIZE) / 2;
    }

    public final void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderContent(graphics, mouseX, mouseY, partialTick, getContentWidth());
        if (hasIcon()) {
            renderIcon(graphics, getIconX(), getIconY(), ICON_SIZE, partialTick);
        }
    }

    protected void renderContent(GenericGraphics graphics, int mouseX, int mouseY, float partialTick, int contentWidth) {
        boolean hovered = isHovered(mouseX, mouseY);
        if (isSelected()) {
            graphics.fillWH(getX(), getY(), getWidth(), getHeight(), 0xFF5865F2);
        } else if (hovered) {
            graphics.fillWH(getX(), getY(), getWidth(), getHeight(), 0x33FFFFFF);
        }
        int color = isSelected() ? 0xFFFFFF00 : 0xFFFFFFFF;
        graphics.drawScrollingString(Minecraft.getInstance().font, displayComponent(),
                getContentX(), getY(), contentWidth, getHeight(), isSelected() || hovered, color);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    @Override public int getX() { return owner.getX(); }
    @Override public int getWidth() { return owner.getWidth(); }
    @Override public int getY() { return y; }
    @Override public void setY(int y) { this.y = y; }
    @Override public int getHeight() { return height; }
    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
}
