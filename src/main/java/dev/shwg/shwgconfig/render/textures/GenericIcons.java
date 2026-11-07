package dev.shwg.shwgconfig.render.textures;

import dev.shwg.shwgconfig.render.GenericGraphics;

public final class GenericIcons {
    private GenericIcons() {
    }

    // Repurposed from vanilla's resource-pack transfer-list arrows. The sprites are defined
    // on an oversized 32x32 source region - only a small glyph within is visible - so draw
    // calls must offset by the glyph's own position inside that region, not its own origin.
    private static final GenericSprite ARROW_DOWN = GenericSprite.changedVanillaTexture(
            "textures/gui/resource_packs.png", 64, 0, 32, 32, 256, 256, "transferable_list/move_down");
    private static final GenericSprite ARROW_DOWN_HIGHLIGHTED = GenericSprite.changedVanillaTexture(
            "textures/gui/resource_packs.png", 64, 32, 32, 32, 256, 256, "transferable_list/move_down_highlighted");
    private static final GenericSprite ARROW_UP = GenericSprite.changedVanillaTexture(
            "textures/gui/resource_packs.png", 96, 0, 32, 32, 256, 256, "transferable_list/move_up");
    private static final GenericSprite ARROW_UP_HIGHLIGHTED = GenericSprite.changedVanillaTexture(
            "textures/gui/resource_packs.png", 96, 32, 32, 32, 256, 256, "transferable_list/move_up_highlighted");

    public static final int SMALL_ARROW_GLYPH_WIDTH = 11;
    public static final int SMALL_ARROW_GLYPH_HEIGHT = 7;
    private static final int SMALL_ARROW_GLYPH_OFFSET_X = 18;
    private static final int SMALL_ARROW_UP_GLYPH_OFFSET_Y = 5;
    private static final int SMALL_ARROW_DOWN_GLYPH_OFFSET_Y = 20;

    /**
     * Draws the small up-arrow from the resourcepack selection screen glyph so its visible triangle lands at (glyphX, glyphY).
     */
    public static void drawArrowUp(GenericGraphics graphics, int glyphX, int glyphY, boolean highlighted) {
        GenericSprite sprite = highlighted ? ARROW_UP_HIGHLIGHTED : ARROW_UP;
        graphics.drawSprite(sprite, glyphX - SMALL_ARROW_GLYPH_OFFSET_X, glyphY - SMALL_ARROW_UP_GLYPH_OFFSET_Y);
    }

    /**
     * Draws the small down-arrow from the resourcepack selection screen glyph so its visible triangle lands at (glyphX, glyphY).
     */
    public static void drawArrowDown(GenericGraphics graphics, int glyphX, int glyphY, boolean highlighted) {
        GenericSprite sprite = highlighted ? ARROW_DOWN_HIGHLIGHTED : ARROW_DOWN;
        graphics.drawSprite(sprite, glyphX - SMALL_ARROW_GLYPH_OFFSET_X, glyphY - SMALL_ARROW_DOWN_GLYPH_OFFSET_Y);
    }

    public static final int LARGE_ARROW_GLYPH_WIDTH = 14;
    public static final int LARGE_ARROW_GLYPH_HEIGHT = 22;
    private static final int LARGE_ARROW_GLYPH_OFFSET_Y = 5;
    private static final int LARGE_ARROW_LEFT_GLYPH_OFFSET_X = 2;
    private static final int LARGE_ARROW_RIGHT_GLYPH_OFFSET_X = 10;

    private static final GenericSprite ARROW_LEFT = GenericSprite.changedVanillaTexture(
            "textures/gui/resource_packs.png", 32, 0, 32, 32, 256, 256, "transferable_list/unselect");

    private static final GenericSprite ARROW_LEFT_HIGHLIGHTED = GenericSprite.changedVanillaTexture(
            "textures/gui/resource_packs.png", 32, 32, 32, 32, 256, 256, "transferable_list/unselect_highlighted");

    private static final GenericSprite ARROW_RIGHT = GenericSprite.changedVanillaTexture(
            "textures/gui/resource_packs.png", 0, 0, 32, 32, 256, 256, "transferable_list/select");

    private static final GenericSprite ARROW_RIGHT_HIGHLIGHTED = GenericSprite.changedVanillaTexture(
            "textures/gui/resource_packs.png", 0, 32, 32, 32, 256, 256, "transferable_list/select_highlighted");

    /**
     * Draws the large left-arrow from the resourcepack selection screen glyph so its visible triangle lands at (glyphX, glyphY).
     */
    public static void drawArrowLeft(GenericGraphics graphics, int glyphX, int glyphY, boolean highlighted) {
        GenericSprite sprite = highlighted ? ARROW_LEFT_HIGHLIGHTED : ARROW_LEFT;
        graphics.drawSprite(sprite, glyphX - LARGE_ARROW_LEFT_GLYPH_OFFSET_X, glyphY - LARGE_ARROW_GLYPH_OFFSET_Y);
    }

    /**
     * Draws the large right-arrow from the resourcepack selection screen glyph so its visible triangle lands at (glyphX, glyphY).
     */
    public static void drawArrowRight(GenericGraphics graphics, int glyphX, int glyphY, boolean highlighted) {
        GenericSprite sprite = highlighted ? ARROW_RIGHT_HIGHLIGHTED : ARROW_RIGHT;
        graphics.drawSprite(sprite, glyphX - LARGE_ARROW_RIGHT_GLYPH_OFFSET_X, glyphY - LARGE_ARROW_GLYPH_OFFSET_Y);
    }

    // Red cross from vanilla's Realms player list ("remove player", 8x7). From 1.20.2 it is the
    // vanilla sprite minecraft:player_list/remove_player. Older versions use the copy of that
    // sprite shipped in this mod's assets (like text_field), drawn as a plain 8x7 texture -
    // the old realms cross_player_icon.png has stray pixels and is not used.
    private static final GenericSprite CROSS = GenericSprite.plain("minecraft", "player_list/remove_player", 8, 7);

    public static final int CROSS_WIDTH = 8;
    public static final int CROSS_HEIGHT = 7;

    /**
     * Draws the small red cross from vanilla's Realms player list with its top left corner at (x, y).
     */
    public static void drawCross(GenericGraphics graphics, int x, int y) {
        graphics.drawSprite(CROSS, x, y);
    }

}
