package dev.shwg.shwgconfig.gui.components.widget;

import dev.shwg.shwgconfig.gui.deferred.overlay.OverlayDispatch;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.gui.layout.Alignment;
import dev.shwg.shwgconfig.gui.layout.GenericVerticalLayout;
import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import dev.shwg.shwgconfig.gui.layout.RenderableElement;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GenericScrollableWidget extends GenericAbstractContainerWidget {

    private static final int SCROLLBAR_SIDE_PADDING = 6;

    private final int backgroundColor;
    private final ScrollBar scrollBar;

    private final GenericVerticalLayout layout;

    private double scrollOffset = 0;

    public GenericScrollableWidget(int x, int y, int width, int height, int backgroundColor, int scrollbarWidth, int scrollbarKnobWidth, int scrollbarColor, int scrollbarKnobColor, int scrollbarKnobHoverColor, GenericTexture scrollbarTexture, GenericTexture knobTexture, GenericTexture knobHoverTexture, int verticalSpacing, int padding, Alignment.Horizontal alignment) {
        super(x, y, width, height, Component.empty());
        int scrollbarOffset = Math.max(scrollbarWidth, scrollbarKnobWidth) + SCROLLBAR_SIDE_PADDING;
        this.layout = new GenericVerticalLayout(x, y, width, height, verticalSpacing, padding, alignment, scrollbarOffset);
        this.backgroundColor = backgroundColor;
        this.scrollBar = new ScrollBar(x + width - SCROLLBAR_SIDE_PADDING, y, scrollbarWidth, scrollbarKnobWidth, height, layout.getContentHeight(), true, scrollbarColor, scrollbarKnobColor, scrollbarKnobHoverColor, scrollbarTexture, knobTexture, knobHoverTexture);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        layout.setX(x);
        updateScrollbar();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        updateContentPosition();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        updateScrollbar();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        layout.setWidth(width);
        updateScrollbar();
    }

    public void add(LayoutElement layoutElement) {
        this.layout.add(layoutElement);
        this.layout.arrange();
    }

    public void remove(LayoutElement layoutElement) {
        this.layout.remove(layoutElement);
        this.layout.arrange();
    }

    private void updateScrollbar() {
        scrollBar.updateScrollbar(getX() + width - SCROLLBAR_SIDE_PADDING, getY(), height, layout.getContentHeight(), getMaxScroll(), true);
    }

    public void visitLayoutWidgets(Consumer<GenericAbstractWidget> consumer) {
        layout.visitWidgets(consumer);
    }

    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (backgroundColor != 0) {
            graphics.fillWH(getX(), getY(), width, height, backgroundColor);
        }
        graphics.enableScissor(getX(), getY(), getWidth(), getHeight());
        boolean blocked = OverlayDispatch.isMouseBlocked(this.children(), mouseX, mouseY) || !isHovered;
        layout.visitChildren(layoutElement -> {
            if (layoutElement instanceof RenderableElement renderableElement && isElementVisible(renderableElement)) {
                renderableElement.render(graphics, blocked ? -1 : mouseX, blocked ? -1 : mouseY, partialTick);
            }
        });
        scrollBar.render(graphics, mouseX, mouseY, scrollOffset);
        graphics.disableScissor();
    }

    private boolean isElementVisible(LayoutElement element) {
        int visibleTop = this.getY();
        int visibleBottom = this.getBottom();
        int elementTop = element.getY();
        int elementBottom = element.getBottom();

        return elementBottom >= visibleTop && elementTop <= visibleBottom;
    }

    @Override
    protected void onChildFocusChanged(@Nullable GuiEventListener newFocused) {
        scrollIntoView(newFocused);
    }

    private void scrollIntoView(GuiEventListener listener) {
        if (!(listener instanceof LayoutElement element)) {
            return;
        }
        int viewTop = getY();
        int viewBottom = getBottom();
        int elementTop = element.getY();
        int elementBottom = element.getBottom();

        if (elementTop < viewTop) {
            scrollOffset -= (viewTop - elementTop);
        } else if (elementBottom > viewBottom) {
            scrollOffset += (elementBottom - viewBottom);
        } else {
            return;
        }
        clampScroll();
        updateContentPosition();
    }

    private void updateContentPosition() {
        layout.setY(getY() - (int) scrollOffset);
        updateScrollbar();
    }

    public int getScrollbarWidth() {
        return scrollBar.getActualBarWidth() + SCROLLBAR_SIDE_PADDING;
    }

    private double getMaxScroll() {
        return Math.max(0, layout.getContentHeight() - getHeight());
    }

    private boolean canScroll() {
        return getMaxScroll() > 0;
    }

    private void clampScroll() {
        scrollOffset = Mth.clamp(scrollOffset, 0, getMaxScroll());
    }

    public void scrollToTop() {
        scrollOffset = 0;
    }

    public void scrollToBottom() {
        scrollOffset = getMaxScroll();
    }

    public void arrange() {
        layout.arrange();
        clampScroll();
        updateScrollbar();
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        List<GenericAbstractWidget> children = new ArrayList<>();
        layout.visitWidgets(children::add);
        return children;
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent event) {
        if (canScroll() && event.isLeftClick() && scrollBar.isHoveringKnob(event.getMouseX(), event.getMouseY(), scrollOffset)) {
            scrollBar.setDragging(true);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseReleased(GenericMouseButtonEvent event) {
        if (scrollBar.isDragging()) {
            scrollBar.setDragging(false);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseDragged(GenericMouseButtonEvent event, double deltaX, double deltaY) {
        if (canScroll() && scrollBar.isDragging()) {
            scrollOffset = scrollBar.drag(event.getMouseY(), deltaY, scrollOffset);
            clampScroll();
            updateContentPosition();
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scrollOffset -= vertical * 10;
        clampScroll();
        updateContentPosition();
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GenericAbstractWidget.Builder<GenericScrollableWidget, GenericScrollableWidget.Builder> {

        private int backgroundColor = 0x4d000000;

        private int scrollbarWidth = 2;
        private int knobWidth = 6;

        private int scrollbarColor = 0xAA000000;
        private int knobColor = 0xFF646464;
        private int knobHoverColor = 0xFFF0F0F0;

        private GenericTexture scrollbarTexture = null;
        private GenericTexture knobTexture = null;
        private GenericTexture knobHoverTexture = null;

        private int spacing = 4;
        private int padding = 4;

        private Alignment.Horizontal alignment = Alignment.Horizontal.CENTER;

        public Builder() {
            super(Component.empty());
        }

        public Builder backgroundColor(int color) {
            this.backgroundColor = color;
            return self();
        }

        public Builder scrollbarWidth(int scrollbar, int knob) {
            this.scrollbarWidth = scrollbar;
            this.knobWidth = knob;
            return self();
        }

        public Builder scrollbarColors(int scrollbar, int knob, int knobHover) {
            this.scrollbarColor = scrollbar;
            this.knobColor = knob;
            this.knobHoverColor = knobHover;
            return self();
        }

        public Builder scrollbarTextures(GenericTexture scrollbar, GenericTexture knob, GenericTexture knobHover) {
            this.scrollbarTexture = scrollbar;
            this.knobTexture = knob;
            this.knobHoverTexture = knobHover;
            return self();
        }

        public Builder spacing(int spacing) {
            this.spacing = spacing;
            return self();
        }

        public Builder padding(int padding) {
            this.padding = padding;
            return self();
        }

        public Builder alignment(Alignment.Horizontal alignment) {
            this.alignment = alignment;
            return self();
        }

        public GenericScrollableWidget build() {
            return new GenericScrollableWidget(x, y, width, height, backgroundColor, scrollbarWidth, knobWidth, scrollbarColor, knobColor, knobHoverColor, scrollbarTexture, knobTexture, knobHoverTexture, spacing, padding, alignment);
        }
    }
}
