package dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox;

import dev.shwg.shwgconfig.gui.deferred.overlay.ListWidgetOverlay;
import dev.shwg.shwgconfig.gui.deferred.overlay.OverlayEntry;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public abstract class SuggestionEntry implements OverlayEntry {

    private static final int ICON_SIZE = 10;
    private static final int ICON_PADDING = 2;

    protected final ListWidgetOverlay<?> owner;
    protected int y;
    private boolean selected;

    public SuggestionEntry(ListWidgetOverlay<?> owner) {
        this.owner = owner;
    }

    public final void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderContent(graphics, mouseX, mouseY, partialTick, getContentWidth());
        if (hasIcon()) {
            renderIcon(graphics, getIconX(), getIconY(), ICON_SIZE, partialTick);
        }
    }

    protected abstract void renderContent(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks, int contentWidth);

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

    protected boolean hasIcon() {
        return false;
    }

    protected final int getContentWidth() {
        int reserved = owner.getScrollbarReservedWidth();
        if (hasIcon()) {
            reserved += ICON_SIZE + ICON_PADDING * 2;
        }
        return getWidth() - reserved;
    }

    /**
     * Left edge where content (text) should start drawing. Sits past the icon when it's
     * on the LEFT side; otherwise sits at the entry's own left padding regardless of
     * whether a RIGHT-side icon is present, since a right icon only affects how much
     * width is available (already handled by {@link #getContentWidth()}), not where
     * text begins.
     */
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
        return getX() + getWidth() - owner.getScrollbarReservedWidth() - ICON_SIZE - ICON_PADDING;
    }

    private int getIconY() {
        return getY() + (getHeight() - ICON_SIZE) / 2;
    }

    public abstract Component displayComponent();

    public String sortingString() {
        return displayComponent().getString();
    }

    public String saveValue() {
        return displayComponent().getString();
    }

    public boolean matchesQuery(String lowerCaseQuery) {
        return sortingString().toLowerCase(Locale.ROOT).contains(lowerCaseQuery);
    }

    public int relevanceTier(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        String key = sortingString().toLowerCase(Locale.ROOT);
        if (key.equals(q)) return 0;
        if (key.startsWith(q)) return 1;
        if (key.contains(q)) return 2;
        return 3;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= y && mouseY < y + getHeight();
    }

    public int getX() {
        return owner.getX();
    }

    public int getY() {
        return y;
    }

    public abstract int getHeight();

    public int getWidth() {
        return owner.getWidth();
    }

    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) { this.selected = selected; }

    public void setY(int y) {
        this.y = y;
    }
}
