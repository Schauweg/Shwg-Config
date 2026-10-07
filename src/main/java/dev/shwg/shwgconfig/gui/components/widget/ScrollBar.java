package dev.shwg.shwgconfig.gui.components.widget;

import dev.shwg.shwgconfig.render.GenericCursors;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import net.minecraft.util.Mth;

public class ScrollBar {
    private final int barWidth, knobWidth, centerOffset;
    private final int scrollbarColor, scrollbarKnobColor, scrollbarKnobHoverColor;
    private final GenericTexture scrollbarTexture, knobTexture, knobHoverTexture;
    private int x, y, height, contentHeight;
    private double scrollableDistance;
    private boolean scrollbarDragging = false;

    public ScrollBar(int x, int y, int barWidth, int knobWidth, int height, int contentHeight) {
        this(x, y, barWidth, knobWidth, height, contentHeight, true, 0xAA000000, 0xFF646464, 0xFFF0F0F0, null, null, null);
    }

    public ScrollBar(int x, int y, int barWidth, int knobWidth, int height, int contentHeight, boolean calculateBarOffset, int scrollbarColor, int scrollbarKnobColor, int scrollbarKnobHoverColor, GenericTexture scrollbarTexture, GenericTexture knobTexture, GenericTexture knobHoverTexture) {
        this.barWidth = barWidth;
        this.knobWidth = knobWidth;
        this.centerOffset = getActualBarWidth() / 2;
        this.x = x - (calculateBarOffset ? getActualBarWidth() : 0);
        this.y = y;
        this.height = height;
        this.contentHeight = contentHeight;
        this.scrollbarColor = scrollbarColor;
        this.scrollbarKnobColor = scrollbarKnobColor;
        this.scrollbarKnobHoverColor = scrollbarKnobHoverColor;
        this.scrollbarTexture = scrollbarTexture;
        this.knobTexture = knobTexture;
        this.knobHoverTexture = knobHoverTexture;
    }

    public void render(GenericGraphics graphics, int mouseX, int mouseY, double scrollOffset) {
        renderBar(graphics);
        renderKnob(graphics, mouseX, mouseY, scrollOffset);
    }

    private void renderBar(GenericGraphics graphics) {
        int x = this.x + centerOffset - barWidth / 2;
        if (scrollbarTexture != null) {
            graphics.drawTexture(scrollbarTexture, x, y, barWidth, height);
        } else {
            graphics.fill(x, y, x + barWidth, y + height, scrollbarColor);
        }
    }

    private void renderKnob(GenericGraphics graphics, int mouseX, int mouseY, double scrollOffset) {
        int x = this.x + centerOffset - knobWidth / 2;
        int knobY = getKnobY(scrollOffset);

        boolean isHovering = isHoveringKnob(mouseX, mouseY, scrollOffset);
        if (isHovering || scrollbarDragging) {
            if (knobHoverTexture != null) {
                graphics.drawTexture(knobHoverTexture, x, knobY, knobWidth, getKnobHeight());
            } else {
                graphics.fill(x, knobY, x + knobWidth, knobY + getKnobHeight(), scrollbarKnobHoverColor);
            }
        } else {
            if (knobTexture != null) {
                graphics.drawTexture(knobTexture, x, knobY, knobWidth, getKnobHeight());
            } else {
                graphics.fill(x, knobY, x + knobWidth, knobY + getKnobHeight(), scrollbarKnobColor);
            }
        }
        if (isHovering || scrollbarDragging) {
            graphics.requestCursor(scrollbarDragging ? GenericCursors.RESIZE_NS : GenericCursors.POINTING_HAND);
        }
    }

    public void setX(int x, boolean calculateBarOffset) {
        this.x = x - (calculateBarOffset ? getActualBarWidth() : 0);
    }

    public int getActualBarWidth() {
        return Math.max(barWidth, knobWidth);
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;
    }

    public void setScrollableDistance(double scrollableDistance) {
        this.scrollableDistance = scrollableDistance;
    }

    public void updateScrollbar(int x, int y, int height, int contentHeight, double scrollableDistance, boolean calculateBarOffset) {
        setX(x, calculateBarOffset);
        setY(y);
        setHeight(height);
        setContentHeight(contentHeight);
        setScrollableDistance(scrollableDistance);
    }

    private int getKnobY(double scrollOffset) {
        if (scrollableDistance == 0) return y;
        return (int) Math.max(y, scrollOffset * (height - getKnobHeight()) / scrollableDistance + y);
    }

    private int getKnobHeight() {
        int minSize = 8;
        if (contentHeight == 0) return minSize;
        return Mth.clamp((int) ((float) (height * height) / contentHeight), minSize, height - 8);
    }

    public boolean isHoveringKnob(double mouseX, double mouseY, double scrollOffset) {
        int knobY = getKnobY(scrollOffset);
        int knobHeight = getKnobHeight();
        int knobX = this.x + centerOffset - knobWidth / 2;
        return mouseX >= knobX && mouseX <= knobX + knobWidth && mouseY >= knobY && mouseY <= knobY + knobHeight;
    }

    public double drag(double mouseY, double dragY, double scrollOffset) {
        if (mouseY < y) {
            return 0;
        } else if (mouseY > y + height) {
            return scrollableDistance;
        } else {
            double percentage = Math.max(1.0, Math.max(1, scrollableDistance) / (this.height - getKnobHeight()));
            return scrollOffset + dragY * percentage;
        }
    }

    public void setDragging(boolean dragging) {
        this.scrollbarDragging = dragging;
    }

    public boolean isDragging() {
        return scrollbarDragging;
    }
}
