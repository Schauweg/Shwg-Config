package dev.shwg.shwgconfig.api.hud;

import dev.shwg.shwgconfig.render.GenericGraphics;

public class HudRenderContext {

    private final GenericGraphics graphics;
    private final int screenWidth;
    private final int screenHeight;
    private final float partialTicks;
    private final boolean debugScreenOpen;

    HudRenderContext(GenericGraphics graphics, int screenWidth, int screenHeight, float partialTicks, boolean debugScreenOpen) {
        this.graphics = graphics;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.partialTicks = partialTicks;
        this.debugScreenOpen = debugScreenOpen;
    }

    public GenericGraphics graphics() {
        return graphics;
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public float partialTicks() {
        return partialTicks;
    }

    public boolean debugScreenOpen() {
        return debugScreenOpen;
    }
}
