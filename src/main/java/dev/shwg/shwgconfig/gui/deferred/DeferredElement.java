package dev.shwg.shwgconfig.gui.deferred;

import dev.shwg.shwgconfig.render.GenericGraphics;

public interface DeferredElement {
    void render(GenericGraphics genericGraphics, int mouseX, int mouseY, float partialTicks);
}
