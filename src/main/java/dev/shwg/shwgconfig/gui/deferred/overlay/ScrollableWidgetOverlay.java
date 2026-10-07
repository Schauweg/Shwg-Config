package dev.shwg.shwgconfig.gui.deferred.overlay;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.components.widget.ScrollBar;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import net.minecraft.util.Mth;

public abstract class ScrollableWidgetOverlay extends WidgetOverlay {

    private static final int BAR_WIDTH = 4;
    private static final int WHEEL_SCROLL_AMOUNT = 10;

    private final ScrollBar scrollBar;
    private double scrollOffset = 0;

    protected ScrollableWidgetOverlay(int width, int height, int contentHeight, Alignment.Horizontal alignment, GenericAbstractWidget carrierWidget) {
        super(width, height, alignment, carrierWidget);
        scrollBar = new ScrollBar(x + width - BAR_WIDTH, y, BAR_WIDTH, BAR_WIDTH, height, contentHeight);
        updateScrollbarGeometry();
    }

    /** Total height of everything scrollable - not just what's currently visible. */
    protected abstract int getContentHeight();

    public int getScrollbarReservedWidth() {
        return BAR_WIDTH + scrollBar.getActualBarWidth();
    }

    /** Fires whenever scrollOffset actually changes. No-op by default - override to
     *  reposition content, same relationship updateDirection() has to its own hook. */
    protected void onScrollChanged() {
    }

    public double getScrollOffset() {
        return scrollOffset;
    }

    public double getMaxScroll() {
        return Math.max(0, getContentHeight() - getHeight());
    }

    public boolean canScroll() {
        return getMaxScroll() > 0;
    }

    public void setScrollOffset(double scrollOffset) {
        this.scrollOffset = scrollOffset;
        clampScroll();
    }

    /** The one place scrollOffset gets clamped - always ends by notifying onScrollChanged(),
     *  so no mutation site can forget to reposition content afterward. */
    protected final void clampScroll() {
        scrollOffset = Mth.clamp(scrollOffset, 0, getMaxScroll());
        onScrollChanged();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        if (scrollBar != null) {
            scrollBar.setX(x + width - scrollBar.getActualBarWidth(), true);
        }
    }

    @Override
    protected void updateDirection() {
        super.updateDirection();
        if (scrollBar != null) {
            updateScrollbarGeometry();
        }
    }

    private void updateScrollbarGeometry() {
        scrollBar.updateScrollbar(getX() + getWidth() - BAR_WIDTH, getY(), getHeight(), getContentHeight(), getMaxScroll(), true);
        clampScroll();
    }

    public void renderScrollbar(dev.shwg.shwgconfig.render.GenericGraphics graphics, int mouseX, int mouseY) {
        if (canScroll()) {
            scrollBar.render(graphics, mouseX, mouseY, scrollOffset);
        }
    }

    @Override
    public final boolean internalMouseClicked(GenericMouseButtonEvent event) {
        if (event.isLeftClick() && scrollBar.isHoveringKnob(event.getMouseX(), event.getMouseY(), scrollOffset)) {
            scrollBar.setDragging(true);
            return true;
        }
        return super.internalMouseClicked(event);
    }

    @Override
    public final boolean internalMouseReleased(GenericMouseButtonEvent event) {
        if (scrollBar.isDragging()) {
            scrollBar.setDragging(false);
            return true;
        }
        return super.internalMouseReleased(event);
    }

    @Override
    public final boolean internalMouseDragged(GenericMouseButtonEvent event, double deltaX, double deltaY) {
        if (scrollBar.isDragging()) {
            scrollOffset = scrollBar.drag(event.getMouseY(), deltaY, scrollOffset);
            clampScroll();
            return true;
        }
        return super.internalMouseDragged(event, deltaX, deltaY);
    }

    @Override
    public final boolean internalMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (isHovered(mouseX, mouseY) && canScroll()) {
            scrollOffset -= vertical * WHEEL_SCROLL_AMOUNT;
            clampScroll();
            return true;
        }
        return super.internalMouseScrolled(mouseX, mouseY, horizontal, vertical);
    }
}
