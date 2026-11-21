package dev.shwg.shwgconfig.api.hud;

import net.minecraft.resources.Identifier;

/**
 * Something a mod draws on the in-game HUD.
 * <p>
 * Register a layer once during client initialization via
 * {@link HudRenderDispatcher#register(Identifier, HudLayer)}
 * or {@link HudRenderDispatcher#register(String, String, HudLayer)}.<br>
 * ShwgConfig then calls {@link #render(HudRenderContext)} every frame, on every
 * supported Minecraft version and loader.
 * <p>
 * Layers are drawn on top of vanilla's HUD, in the order they were registered,
 * and are skipped while the HUD is hidden (F1).
 * <p>
 * Each layer gets its own pose: transforms you apply to
 * {@link HudRenderContext#graphics()} (translate, scale, rotate) don't affect
 * other layers. If a layer throws an exception, it is logged once and the layer
 * is disabled until the game restarts. Other layers keep rendering.
 * <p>
 * Example:
 * <pre>{@code
 * HudRenderDispatcher.register("mymod", "extrahud", ctx -> {
 *     int x = ctx.screenWidth() - 100;
 *     ctx.graphics().drawString(...);
 * });
 * }</pre>
 */
@FunctionalInterface
public interface HudLayer {

    /**
     * Draws this layer. Called once per frame.
     *
     * @param context the graphics to draw with, the scaled screen size and the partial tick
     */
    void render(HudRenderContext context);
}
