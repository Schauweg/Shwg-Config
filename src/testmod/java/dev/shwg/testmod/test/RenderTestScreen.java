package dev.shwg.testmod.test;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.gui.components.screens.GenericScreen;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.GenericSprite;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import dev.shwg.shwgconfig.render.textures.NineSlicedSprite;
import dev.shwg.shwgconfig.util.DisplayItemStack;
import dev.shwg.testmod.TestMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Visual smoke test for {@link GenericGraphics} and the texture/sprite classes.
 *
 * <p>Every cell has a title and an "expected" hint, so a wrong result is visible at a glance and
 * screenshots from different Minecraft versions can be compared side by side.
 * Controls: LEFT/RIGHT or PAGE UP/DOWN or 1-6 to switch pages, ESC to close.</p>
 *
 * <p>Needs the test sprites in {@code assets/<NS>/textures/gui/sprites/rendertest/}
 * (nine_slice, tiled, stretch - each with a .png.mcmeta).</p>
 */
public class RenderTestScreen extends GenericScreen {

    private static final String[] PAGES = {"Primitives", "Text", "Clip & pose", "Items", "Textures", "Sprites"};

    // grid layout: 2 columns x 3 rows of cells
    private static final int COLS = 2, CELL_W = 226, CELL_H = 76, GAP = 4, ORIGIN_X = 10, ORIGIN_Y = 24;

    private static final int WHITE = 0xFFFFFFFF, BLACK = 0xFF000000, GRAY = 0xFF808080;
    private static final int RED = 0xFFFF4040, GREEN = 0xFF40FF40, BLUE = 0xFF4080FF, YELLOW = 0xFFFFFF40;
    private static final int BG = 0xFF101010, CELL_BG = 0xFF222222, CELL_BORDER = 0xFF505050;
    private static final int BAR = 0xFF404040, BAR_HOVER = 0xFF606020;
    private static final int TITLE = 0xFFFFFFFF, HINT = 0xFF909090;

    private Runnable tickHook;

    private int page = 0;
    private int mx, my;       // current mouse position, for hover tests
    private int cx, cy;       // content origin of the cell most recently started with cell()

    private GenericTexture appleTex, stoneTex, inventoryTex;
    private GenericSprite stretchSprite, tiledSprite, nineSliceSprite;
    private String spriteError;

    public RenderTestScreen(Screen parent) {
        super(Component.literal("Render Test"), parent);
    }

    @Override
    protected void init() {
        super.init();
        // Sprites must be created after resources are loaded, so not in the constructor.
        appleTex = GenericTexture.fromItem(Items.APPLE);
        stoneTex = GenericTexture.fromBlock(Blocks.STONE);
        inventoryTex = GenericTexture.vanillaTexture("textures/gui/container/dispenser.png", 256, 256);
        spriteError = null;
        try {
            stretchSprite = GenericSprite.fromMcMeta(TestMod.MOD_ID, "rendertest/stretch");
            tiledSprite = GenericSprite.fromMcMeta(TestMod.MOD_ID, "rendertest/tiled");
            nineSliceSprite = GenericSprite.fromMcMeta(TestMod.MOD_ID, "rendertest/nine_slice");
        } catch (RuntimeException e) {
            spriteError = e.toString();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (tickHook != null) tickHook.run();
    }

    // Input 

    @Override
    protected boolean onKeyPressed(GenericKeyEvent e) {
        int key = e.key();
        if (e.isRight() || key == InputConstants.KEY_PAGEDOWN) {
            page = (page + 1) % PAGES.length;
            return true;
        }
        if (e.isLeft() || key == InputConstants.KEY_PAGEUP) {
            page = (page + PAGES.length - 1) % PAGES.length;
            return true;
        }
        if (key >= InputConstants.KEY_1 && key < InputConstants.KEY_1 + PAGES.length) {
            page = key - InputConstants.KEY_1;
            return true;
        }
        return false;
    }

    // Render 

    @Override
    public void render(GenericGraphics g, int mouseX, int mouseY, float partialTicks) {
        this.mx = mouseX;
        this.my = mouseY;
        Font font = Minecraft.getInstance().font;

        g.fill(0, 0, width, height, BG);
        g.drawString(font, "Render test  [" + (page + 1) + "/" + PAGES.length + "] " + PAGES[page], 10, 8, TITLE);
        String controls = "</>, 1-" + PAGES.length + ": page   ESC: close";
        g.drawString(font, controls, width - font.width(controls) - 10, 8, HINT, false);

        switch (page) {
            case 0 -> pagePrimitives(g, font);
            case 1 -> pageText(g, font);
            case 2 -> pageClipAndPose(g, font);
            case 3 -> pageItems(g, font);
            case 4 -> pageTextures(g, font);
            case 5 -> pageSprites(g, font);
        }

        if (spriteError != null) {
            g.drawString(font, "Sprite load error: " + spriteError, 10, height - 10, RED, false);
        }
    }

    // Page 1: primitives 

    private void pagePrimitives(GenericGraphics g, Font font) {
        cell(g, font, 0, "fill / fillWH / alpha", "red = green size; tint over white and dark");
        g.fill(cx, cy, cx + 20, cy + 20, RED);
        g.fillWH(cx + 25, cy, 20, 20, GREEN);
        g.fillWH(cx + 55, cy, 40, 20, WHITE);
        g.fill(cx + 75, cy, cx + 115, cy + 20, 0x80FF0000);

        cell(g, font, 1, "fillGradient", "top to bottom; last one fades out");
        g.fillGradient(cx, cy, cx + 30, cy + 40, WHITE, BLACK);
        g.fillGradient(cx + 40, cy, cx + 70, cy + 40, RED, BLUE);
        g.fillGradient(cx + 80, cy, cx + 110, cy + 40, 0xFFFFFFFF, 0x00FFFFFF);

        cell(g, font, 2, "hLine / vLine", "reversed endpoints: same length as normal");
        g.hLine(cx, cx + 60, cy + 2, WHITE);
        g.hLine(cx + 60, cx, cy + 6, RED);
        g.vLine(cx + 80, cy, cy + 40, WHITE);
        g.vLine(cx + 85, cy + 40, cy, RED);

        cell(g, font, 3, "outline vs 4x fillWH", "left all green, middle all red, no leaks");
        manualOutline(g, cx, cy, 24, 24, RED);
        g.outline(cx, cy, 24, 24, GREEN);              // drawn over the manual one: red leaking = manual is bigger
        g.outline(cx + 30, cy, 24, 24, GREEN);
        manualOutline(g, cx + 30, cy, 24, 24, RED);    // drawn over the real one: green leaking = outline is bigger
        g.outline(cx + 60, cy, 1, 1, WHITE);
        g.outline(cx + 66, cy, 2, 2, WHITE);
        g.outline(cx + 74, cy, 3, 3, WHITE);

        cell(g, font, 4, "fill: reversed / empty", "2 equal squares, nothing else");
        g.fill(cx, cy, cx + 20, cy + 20, RED);
        g.fill(cx + 50, cy + 20, cx + 30, cy, GREEN);  // corners given in reverse order
        g.fill(cx + 60, cy, cx + 60, cy + 20, WHITE);  // zero width: must draw nothing
        g.fill(cx + 70, cy + 10, cx + 90, cy + 10, WHITE); // zero height: must draw nothing

        cell(g, font, 5, "alpha stacking", "blend order red < green < blue");
        g.fillWH(cx, cy, 30, 30, 0x80FF0000);
        g.fillWH(cx + 15, cy + 8, 30, 30, 0x8000FF00);
        g.fillWH(cx + 30, cy, 30, 30, 0x800000FF);
    }

    private void manualOutline(GenericGraphics g, int x, int y, int w, int h, int color) {
        g.fillWH(x, y, w, 1, color);
        g.fillWH(x, y + h - 1, w, 1, color);
        g.fillWH(x, y, 1, h, color);
        g.fillWH(x + w - 1, y, 1, h, color);
    }

    // Page 2: text 

    private void pageText(GenericGraphics g, Font font) {
        cell(g, font, 0, "String / Component / Sequence", "4 lines, the last one has no shadow");
        g.drawString(font, "String", cx, cy, WHITE);
        g.drawString(font, Component.literal("Component"), cx, cy + 10, WHITE);
        g.drawString(font, Component.literal("Formatted sequence").getVisualOrderText(), cx, cy + 20, WHITE);
        g.drawString(font, "No shadow", cx, cy + 30, WHITE, false);

        cell(g, font, 1, "drawCenteredString", "red line bisects all three");
        int c = cx + 90;
        g.vLine(c, cy - 2, cy + 38, RED);
        g.drawCenteredString(font, "String", c, cy, WHITE);
        g.drawCenteredString(font, Component.literal("Component"), c, cy + 12, WHITE);
        g.drawCenteredString(font, Component.literal("Sequence").getVisualOrderText(), c, cy + 24, WHITE);

        cell(g, font, 2, "drawScrollingString (always)", "long scrolls, short stays still");
        g.fillWH(cx, cy - 1, 100, 10, BAR);
        g.drawScrollingString(font, "A long text that has to scroll back and forth", cx, cy, 100, WHITE);
        g.fillWH(cx, cy + 14, 100, 10, BAR);
        g.drawScrollingString(font, "Short (static)", cx, cy + 15, 100, WHITE);
        g.fillWH(cx + 110, cy - 1, 60, 10, BAR);
        g.drawScrollingString(font, "Narrow 60px box, long text", cx + 110, cy, 60, WHITE);

        cell(g, font, 3, "scrolling, only while hovered", "static + clipped until hovered");
        boolean h1 = hover(cx, cy - 1, 100, 10);
        g.fillWH(cx, cy - 1, 100, 10, h1 ? BAR_HOVER : BAR);
        g.drawScrollingString(font, "Hover me to scroll this long text", cx, cy, 100, h1, WHITE);
        boolean h2 = hover(cx, cy + 14, 100, 10);
        g.fillWH(cx, cy + 14, 100, 10, h2 ? BAR_HOVER : BAR);
        g.drawScrollingString(font, "Another long text for the second bar", cx, cy + 15, 100, h2, WHITE);

        cell(g, font, 4, "scrolling in a taller row", "text vertically centered in bar");
        g.fillWH(cx, cy, 100, 24, BAR);
        g.drawScrollingString(font, "Centered in a 24px row, long text", cx, cy, 100, 24, WHITE);
        g.fillWH(cx + 110, cy, 100, 24, BAR);
        g.drawScrollingString(font, "Short, centered", cx + 110, cy, 100, 24, WHITE);

        cell(g, font, 5, "text alpha (label | sample)", "visible: FF 80 20 - invisible: 04 03 01 00");
        alphaRow(g, font, cx, cy, 0xFF);
        alphaRow(g, font, cx, cy + 10, 0x80);
        alphaRow(g, font, cx, cy + 20, 0x20);
        alphaRow(g, font, cx, cy + 30, 0x04);
        alphaRow(g, font, cx + 110, cy, 0x03);
        alphaRow(g, font, cx + 110, cy + 10, 0x01);
        alphaRow(g, font, cx + 110, cy + 20, 0x00);
        g.drawString(font, "Opaque, no shadow", cx + 110, cy + 30, 0xFFFFFFFF, false);
    }

    /** Gray label with the alpha value, then a red sample text drawn with exactly that alpha. */
    private void alphaRow(GenericGraphics g, Font font, int x, int y, int alpha) {
        g.drawString(font, String.format("%02X", alpha), x, y, HINT, false);
        g.drawString(font, "Sample text", x + 16, y, (alpha << 24) | 0xFF0000);
    }

    // Page 3: clipping and pose 

    private void pageClipAndPose(GenericGraphics g, Font font) {
        cell(g, font, 0, "scissor (single)", "red only inside white box; blue visible");
        g.enableScissorWH(cx, cy, 40, 30);
        g.fill(cx - 10, cy - 6, cx + 60, cy + 36, RED);
        g.disableScissor();
        g.outline(cx, cy, 40, 30, WHITE);
        g.fillWH(cx + 50, cy, 8, 8, BLUE);             // drawn after release: must not be clipped

        cell(g, font, 1, "scissor (nested)", "green only in white box; blue to x+50");
        g.enableScissorWH(cx, cy, 50, 30);
        g.enableScissorWH(cx + 30, cy + 10, 50, 30);
        g.fill(cx - 10, cy - 6, cx + 90, cy + 40, GREEN);
        g.disableScissor();
        g.fill(cx - 10, cy + 2, cx + 90, cy + 6, BLUE); // only the outer scissor is active here
        g.disableScissor();
        g.outline(cx + 30, cy + 10, 20, 20, WHITE);

        cell(g, font, 2, "scissor inside translated pose", "green fills box = follows pose; none = raw");
        g.pushPose();
        g.translate(60, 0);
        g.outline(cx, cy, 40, 24, WHITE);              // where the clip lands if scissor follows the pose
        g.enableScissorWH(cx, cy, 40, 24);
        g.fill(cx - 20, cy - 6, cx + 100, cy + 30, GREEN);
        g.disableScissor();
        g.popPose();

        cell(g, font, 3, "translate / scale", "gray, red, green, blue all 20x20");
        g.outline(cx, cy, 20, 20, GRAY);
        g.pushPose();
        g.translate(cx + 30, cy);
        g.fillWH(0, 0, 20, 20, RED);
        g.popPose();
        g.pushPose();
        g.translate(cx + 60, cy);
        g.scale(2f, 2f);
        g.fillWH(0, 0, 10, 10, GREEN);
        g.popPose();
        g.pushPose();
        g.translate(cx + 100, cy);
        g.scale(0.5f, 0.5f);
        g.fillWH(0, 0, 40, 40, BLUE);
        g.popPose();
        g.fillWH(cx + 130, cy, 20, 20, YELLOW);        // after all pops: untransformed

        cell(g, font, 4, "rotate", "pivot dot centered; red corner moves clockwise");
        g.pushPose();
        g.translate(cx + 25, cy + 25);
        g.rotate(rad(45));
        g.fillWH(-12, -12, 24, 24, BLUE);
        g.fillWH(6, -12, 6, 6, RED);                   // top-right corner marker
        g.popPose();
        g.fillWH(cx + 24, cy + 24, 2, 2, WHITE);
        g.pushPose();
        g.translate(cx + 90, cy + 25);
        g.rotate(rad(30));
        g.fillWH(-12, -12, 24, 24, GREEN);
        g.fillWH(6, -12, 6, 6, RED);
        g.popPose();

        cell(g, font, 5, "nested push / pop", "4 squares, evenly spaced 15px");
        g.pushPose();
        g.translate(cx, cy);
        g.fillWH(0, 0, 10, 10, RED);
        g.pushPose();
        g.translate(15, 0);
        g.fillWH(0, 0, 10, 10, GREEN);
        g.popPose();
        g.fillWH(30, 0, 10, 10, BLUE);
        g.popPose();
        g.fillWH(cx + 45, cy, 10, 10, WHITE);
    }

    // Page 4: items 

    private void pageItems(GenericGraphics g, Font font) {
        ItemStack sword = DisplayItemStack.getStackSimple(Items.DIAMOND_HELMET);
        ItemStack damaged = DisplayItemStack.builder(Items.DIAMOND_SWORD).withDamage(500, DisplayItemStack.DIAMOND_TOOL_MAX_DAMAGE).build();

        cell(g, font, 0, "renderItem / renderDecoratedItem", "sword, apple 7, durability, '99', 2x emerald");
        g.renderItem(sword, cx, cy);
        g.renderDecoratedItem(font, DisplayItemStack.getStackSimple(Items.APPLE, 7), cx + 24, cy);
        g.renderDecoratedItem(font, damaged, cx + 48, cy);
        g.renderDecoratedItem(font, sword, cx + 72, cy, "99");
        g.renderItem(Items.EMERALD, cx + 96, cy);
        g.renderDecoratedItem(font, Items.EMERALD, cx + 120, cy);

        cell(g, font, 1, "renderScaledItem", "sizes 8 / 16 / 24 / 32");
        g.renderScaledItem(sword, cx, cy, 8);
        g.renderScaledItem(sword, cx + 14, cy, 16);
        g.renderScaledItem(sword, cx + 36, cy, 24);
        g.renderScaledItem(sword, cx + 66, cy, 32);

        cell(g, font, 2, "item vs later fill (z-order)", "green must cover the right half of the item");
        g.fillWH(cx, cy, 40, 20, RED);
        g.renderItem(sword, cx + 4, cy + 2);
        g.fill(cx + 12, cy - 4, cx + 50, cy + 24, 0x8000FF00);

        cell(g, font, 3, "item under pose", "rotated 20deg / scaled 1.5x");
        g.pushPose();
        g.translate(cx + 14, cy + 20);
        g.rotate(rad(20));
        g.renderItem(sword, -8, -8);
        g.popPose();
        g.pushPose();
        g.translate(cx + 60, cy);
        g.scale(1.5f, 1.5f);
        g.renderItem(sword, 0, 0);
        g.popPose();

        cell(g, font, 4, "item inside scissor", "only the top-left 8x8 of the item");
        g.enableScissorWH(cx, cy, 8, 8);
        g.renderItem(sword, cx, cy);
        g.disableScissor();
        g.outline(cx - 1, cy - 1, 10, 10, WHITE);
        g.renderItem(sword, cx + 30, cy);              // after release: complete
    }

    private static int getInt() {
        return 34;
    }

    // Page 5: plain textures 

    private void pageTextures(GenericGraphics g, Font font) {
        cell(g, font, 0, "drawTexture (item/block)", "natural, 2x, 0.5x; stone natural, 40x40");
        g.drawTexture(appleTex, cx, cy);
        g.drawTexture(appleTex, cx + 24, cy, 32, 32);
        g.drawTexture(appleTex, cx + 64, cy, 8, 8);
        g.drawTexture(stoneTex, cx + 80, cy);
        g.drawTexture(stoneTex, cx + 104, cy, 40, 40);

        cell(g, font, 1, "whole texture, scaled", "inventory.png at 48x48 and 96x48");
        g.drawTexture(inventoryTex, cx, cy, 48, 48);
        g.drawTexture(inventoryTex, cx + 60, cy, 96, 48);

        cell(g, font, 2, "blit: sub-region", "inventory slot frame: 1x, 2x, wide");
        g.blit(inventoryTex, cx, cy, 7f, 83f, 18, 18, 18, 18);
        g.blit(inventoryTex, cx + 30, cy, 7f, 83f, 36, 36, 18, 18);
        g.blit(inventoryTex, cx + 76, cy, 7f, 83f, 54, 18, 18, 18);

        cell(g, font, 3, "drawTextureRepeating", "tiles, last tile cropped, never stretched");
        g.drawTextureRepeating(stoneTex, cx, cy, 40, 40);
        g.drawTextureRepeating(stoneTex, cx + 50, cy, 16, 16);
        g.drawTextureRepeating(stoneTex, cx + 76, cy, 70, 20);

        cell(g, font, 4, "texture under pose", "rotated 30deg / scaled 2x");
        g.pushPose();
        g.translate(cx + 20, cy + 22);
        g.rotate(rad(30));
        g.drawTexture(appleTex, -16, -16, 32, 32);
        g.popPose();
        g.pushPose();
        g.translate(cx + 70, cy);
        g.scale(2f, 2f);
        g.drawTexture(appleTex, 0, 0);
        g.popPose();

        cell(g, font, 5, "texture inside scissor", "top-left 16x16 of the 32x32 only");
        g.enableScissorWH(cx, cy, 16, 16);
        g.drawTexture(appleTex, cx, cy, 32, 32);
        g.disableScissor();
        g.drawTexture(appleTex, cx + 40, cy, 32, 32);  // after release: complete
    }

    // Page 6: sprites 

    private void pageSprites(GenericGraphics g, Font font) {
        if (stretchSprite == null || tiledSprite == null || nineSliceSprite == null) {
            g.drawString(font, "Sprites not loaded (see error below / check the assets)", 10, 30, RED);
            return;
        }

        cell(g, font, 0, "stretch: " + name(stretchSprite), "L stays upright, only gets stretched");
        g.drawTexture(stretchSprite, cx, cy);
        g.drawTexture(stretchSprite, cx + 24, cy, 48, 16);
        g.drawTexture(stretchSprite, cx + 80, cy, 16, 48);
        g.drawTexture(stretchSprite, cx + 104, cy, 48, 48);

        cell(g, font, 1, "tiled: " + name(tiledSprite), "8px tiles repeat, edges cropped");
        g.drawTexture(tiledSprite, cx, cy);
        g.drawTexture(tiledSprite, cx + 16, cy, 20, 20);
        g.drawTexture(tiledSprite, cx + 44, cy, 60, 40);

        cell(g, font, 2, "nine-slice: " + name(nineSliceSprite), "corners R/G/B/Y keep size (L3 T5 R7 B4)");
        g.drawTexture(nineSliceSprite, cx, cy);
        g.drawTexture(nineSliceSprite, cx + 30, cy, 60, 24);
        g.drawTexture(nineSliceSprite, cx + 96, cy, 24, 48);
        g.drawTexture(nineSliceSprite, cx + 126, cy, 50, 40);

        cell(g, font, 3, "nine-slice edge cases", "8x8 < borders, same w, same h, native, direct");
        g.drawTexture(nineSliceSprite, cx, cy, 8, 8);
        g.drawTexture(nineSliceSprite, cx + 14, cy, 24, 40);
        g.drawTexture(nineSliceSprite, cx + 44, cy, 60, 24);
        g.drawTexture(nineSliceSprite, cx + 110, cy, 24, 24);
        if (nineSliceSprite instanceof NineSlicedSprite nine) {
            g.drawNineSliced(nine, cx + 140, cy, 40, 30);
        }

        cell(g, font, 4, "sprites under pose", "2x scaled / rotated 15deg");
        g.pushPose();
        g.translate(cx, cy);
        g.scale(2f, 2f);
        g.drawTexture(nineSliceSprite, 0, 0, 30, 20);
        g.popPose();
        g.pushPose();
        g.translate(cx + 120, cy + 24);
        g.rotate(rad(15));
        g.drawTexture(nineSliceSprite, -20, -12, 40, 24);
        g.popPose();

        cell(g, font, 5, "sprites inside scissor", "only the top-left 30x20 of each");
        g.enableScissorWH(cx, cy, 30, 20);
        g.drawTexture(nineSliceSprite, cx, cy, 60, 40);
        g.disableScissor();
        g.enableScissorWH(cx + 70, cy, 30, 20);
        g.drawTexture(tiledSprite, cx + 60, cy - 10, 60, 40);
        g.disableScissor();
    }

    // Helpers 

    /**
     * Draws the cell frame, title and expectation hint, and sets {@link #cx}/{@link #cy} to its content origin.
     */
    private void cell(GenericGraphics g, Font font, int index, String title, String hint) {
        int x = ORIGIN_X + (index % COLS) * (CELL_W + GAP);
        int y = ORIGIN_Y + (index / COLS) * (CELL_H + GAP);
        g.fillWH(x, y, CELL_W, CELL_H, CELL_BG);
        g.outline(x, y, CELL_W, CELL_H, CELL_BORDER);
        g.drawString(font, title, x + 4, y + 3, TITLE, false);
        g.drawString(font, hint, x + 4, y + CELL_H - 10, HINT, false);
        this.cx = x + 6;
        this.cy = y + 14;
    }

    private boolean hover(int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static float rad(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    private static String name(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    public void setTickHook(Runnable hook) {
        this.tickHook = hook;
    }

    public int pageCount() {
        return PAGES.length;
    }

    public void setPage(int page) {
        this.page = Math.floorMod(page, PAGES.length);
    }

    public String pageSlug(int index) {
        return PAGES[index].toLowerCase(java.util.Locale.ROOT).replace(" & ", "-").replace(' ', '-');
    }
}
