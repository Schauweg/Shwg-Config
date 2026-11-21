package dev.shwg.shwgconfig.api.hud;

public final class HudOptions {

    private boolean hideWithDebugScreen = false;

    private HudOptions() {

    }

    public static HudOptions create() {
        return new HudOptions();
    }

    public HudOptions hideWithDebugScreen() {
        this.hideWithDebugScreen = true;
        return this;
    }

    boolean isHiddenWithDebugScreen() {
        return hideWithDebugScreen;
    }
}
