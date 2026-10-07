package dev.shwg.shwgconfig.gui.deferred.overlay;

import dev.shwg.shwgconfig.render.GenericGraphics;

public interface OverlayEntry {
    int getX();
    int getY();
    void setY(int y);
    int getWidth();
    int getHeight();
    boolean isHovered(double mouseX, double mouseY);
    boolean isSelected();
    void setSelected(boolean selected);
    void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTick);
}
