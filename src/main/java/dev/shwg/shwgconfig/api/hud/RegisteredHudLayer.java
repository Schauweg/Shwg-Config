package dev.shwg.shwgconfig.api.hud;

class RegisteredHudLayer {
    final HudLayer layer;
    final HudOptions options;

    RegisteredHudLayer(HudLayer layer, HudOptions options) {
        this.layer = layer;
        this.options = options;
    }
}
