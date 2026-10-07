package dev.shwg.shwgconfig.render;

import dev.shwg.shwgconfig.gui.components.screens.GenericScreen;
import dev.shwg.shwgconfig.gui.deferred.DeferredElement;
import dev.shwg.shwgconfig.gui.deferred.GenericTooltip;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.layout.LayoutElement;
import dev.shwg.shwgconfig.render.textures.*;
import dev.shwg.shwgconfig.util.DisplayItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*? if >=1.21.11 {*/
import net.minecraft.util.Util;
/*?} else {*/
//import net.minecraft.Util;
/*?}*/

/*? if >=1.21.6 {*/
import net.minecraft.client.renderer.RenderPipelines;
/*?}*/

/*? if <=1.21.5 && >=1.21.2 {*/
//import net.minecraft.client.renderer.RenderType;
        /*?}*/

/*? if >=1.20 {*/
import net.minecraft.client.gui.GuiGraphicsExtractor;
/*?} else {*/
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.Screen;
*//*?}*/

/*? if <1.19.1 { */
//import dev.shwg.shwgconfig.render.scissor.ScissorStack;
/*?} */

/**
 * Cross-version abstraction layer over Minecraft's GUI rendering API.
 *
 * <p>Wraps the version-specific rendering context - {@code GuiGraphicsExtractor} for
 * Minecraft 1.20 and above, {@code PoseStack} for older versions - behind a
 * single unified interface. Rendering code written against {@code GenericGraphics}
 * compiles correctly for every Minecraft version supported by Stonecutter without
 * requiring version-specific branches at the call site.</p>
 *
 * <p>All {@code color} parameters are ARGB-packed integers in the format
 * {@code 0xAARRGGBB}. An alpha of {@code 0xFF} is fully opaque;
 * {@code 0x00} is fully transparent.</p>
 *
 * @see GenericTexture
 * @see GenericScreen
 * @see GenericAbstractWidget
 */
public class GenericGraphics {

    List<DeferredElement> deferredElements;

    /*? if >=1.20 { */
    private final GuiGraphicsExtractor graphics;

    /**
     * Creates a {@code GenericGraphics} instance backed by the given {@code GuiGraphicsExtractor}.
     * This constructor is used for Minecraft 1.20 and above.
     *
     * @param guiGraphics the native rendering context provided by the current render frame
     **/
    public GenericGraphics(@NotNull GuiGraphicsExtractor guiGraphics) {
        this.graphics = guiGraphics;
        this.deferredElements = new ArrayList<>();
    }

    /**
     * Returns the underlying {@code GuiGraphicsExtractor} instance (Minecraft 1.20+).
     *
     * <p>Prefer the wrapper methods on this class over calling the native API directly,
     * as direct usage bypasses cross-version compatibility.</p>
     *
     * @return the native rendering context
     **/
    public GuiGraphicsExtractor getGraphics() {
        return this.graphics;
    }
    /*?} else {*/
    /*private final PoseStack poseStack;

    /^*
     * Creates a {@code GenericGraphics} instance backed by the given {@code PoseStack}.
     * This constructor is used for Minecraft versions below 1.20.
     *
     * @param poseStack the matrix stack provided by the current render frame
     ^/
    public GenericGraphics(@NotNull PoseStack poseStack) {
        this.poseStack = poseStack;
        this.deferredElements = new ArrayList<>();
    }

    /^*
     * Returns the underlying {@code PoseStack} instance (Minecraft below 1.20).
     *
     * <p>Prefer the wrapper methods on this class over calling the native API directly,
     * as direct usage bypasses cross-version compatibility.</p>
     *
     * @return the native matrix stack
     ^/
    public PoseStack getGraphics() {
        return this.poseStack;
    }
    *//*?} */

    public void renderTooltip(@Nullable GenericTooltip tooltip, int mouseX, int mouseY) {
        if (tooltip == null || tooltip.isEmpty()) return;

        Optional<TooltipComponent> image = tooltip.hasIcon() ? tooltip.getIcon().getTooltipImage() : Optional.empty();

        /*? if >=1.21.6 { */
        Font font = Minecraft.getInstance().font;
        graphics.setTooltipForNextFrame(font, tooltip.getLines(), image, mouseX, mouseY);
        /*?} else if >=1.20 { */
        /*Font font = Minecraft.getInstance().font;
        graphics.renderTooltip(font, tooltip.getLines(), image, mouseX, mouseY);
        *//*?} else { */
        /*Screen screen = Minecraft.getInstance().gui.screen();
        if (screen != null) {
            screen.renderTooltip(poseStack, tooltip.getLines(), image, mouseX, mouseY);
        }
        *//*?}*/
    }

    public void addDeferred(DeferredElement deferredElement) {
        if (deferredElement == null) return;
        this.deferredElements.add(deferredElement);
    }

    public void renderDeferred(int mouseX, int mouseY, float partialTicks) {
        /*? if <1.21.6 {*/
        /*//fix because items were being rendered above overlay
        translate(0, 0, +500);
        *//*?}*/
        for (DeferredElement deferredElement : this.deferredElements) {
            if (!(deferredElement instanceof GenericTooltip)) {
                deferredElement.render(this, mouseX, mouseY, partialTicks);
            }
        }
        //Always render tooltip last
        for (DeferredElement deferredElement : this.deferredElements) {
            if (deferredElement instanceof GenericTooltip tooltip) {
                tooltip.render(this, mouseX, mouseY, partialTicks);
            }
        }
        /*? if <1.21.6 {*/
        /*//undo fix
        translate(0, 0, -500);
        *//*?}*/
    }

    // ── Fill ──────────────────────────────────────────────────────────────────

    /**
     * Fills an axis-aligned rectangle with a solid color.
     *
     * @param x1    left edge of the rectangle (inclusive)
     * @param y1    top edge of the rectangle (inclusive)
     * @param x2    right edge of the rectangle (exclusive)
     * @param y2    bottom edge of the rectangle (exclusive)
     * @param color ARGB fill color ({@code 0xAARRGGBB})
     */
    public void fill(int x1, int y1, int x2, int y2, int color) {
        /*? if >=1.20 {*/
        graphics.fill(x1, y1, x2, y2, color);
        /*?} else {*/
        //GuiComponent.fill(poseStack, x1, y1, x2, y2, color);
        /*?}*/
    }

    /**
     * Fills an axis-aligned rectangle with a solid color.
     *
     * @param x      left edge of the rectangle (inclusive)
     * @param y      top edge of the rectangle (inclusive)
     * @param width  width of the rectangle
     * @param height bottom edge of the rectangle
     * @param color  ARGB fill color ({@code 0xAARRGGBB})
     */
    public void fillWH(int x, int y, int width, int height, int color) {
        fill(x, y, x + width, y + height, color);
    }

    /**
     * Fills an axis-aligned rectangle with a vertical linear color gradient.
     * The color transitions from {@code colorTop} at the top edge to
     * {@code colorBottom} at the bottom edge.
     *
     * @param x0          left edge of the rectangle
     * @param y0          top edge of the rectangle
     * @param x1          right edge of the rectangle
     * @param y1          bottom edge of the rectangle
     * @param colorTop    ARGB color at the top edge ({@code 0xAARRGGBB})
     * @param colorBottom ARGB color at the bottom edge ({@code 0xAARRGGBB})
     */
    public void fillGradient(int x0, int y0, int x1, int y1, int colorTop, int colorBottom) {
        /*? if >=1.20 {*/
        graphics.fillGradient(x0, y0, x1, y1, colorTop, colorBottom);
        /*?} else {*/
        //GuiComponentAccessor.gradient(poseStack, x0, y0, x1, y1, colorTop, colorBottom);
        /*?}*/
    }


    // ── Lines ─────────────────────────────────────────────────────────────────

    /**
     * Draws a one-pixel-tall horizontal line from {@code x0} to {@code x1} at height {@code y}.
     * The endpoints are sorted automatically, so the order of {@code x0} and {@code x1}
     * does not matter.
     *
     * @param x0    start x coordinate
     * @param x1    end x coordinate
     * @param y     vertical position of the line
     * @param color ARGB line color ({@code 0xAARRGGBB})
     */
    public void hLine(int x0, int x1, int y, int color) {
        /*? if >=26 {*/
        graphics.horizontalLine(x0, x1, y, color);
        /*?} else if >=1.20 { */
        //graphics.hLine(x0, x1, y, color);
        /*?} else { */
        //GuiComponentAccessor.shLine(poseStack, x0, x1, y, color);
        /*?} */
    }

    /**
     * Draws a one-pixel-wide vertical line from {@code y0} to {@code y1} at column {@code x}.
     * The endpoints are sorted automatically, so the order of {@code y0} and {@code y1}
     * does not matter.
     *
     * @param x     horizontal position of the line
     * @param y0    start y coordinate
     * @param y1    end y coordinate
     * @param color ARGB line color ({@code 0xAARRGGBB})
     */
    public void vLine(int x, int y0, int y1, int color) {
        /*? if >=26 {*/
        graphics.verticalLine(x, y0, y1, color);
        /*?} else if >=1.20 { */
        //graphics.vLine(x, y0, y1, color);
        /*?} else { */
        //GuiComponentAccessor.svLine(poseStack, x, y0, y1, color);
        /*?} */
    }

    /**
     * Draws a one-pixel-wide rectangular border without filling the interior.
     * Equivalent to four {@link #fill} calls along the edges of the rectangle.
     *
     * @param x      left edge of the rectangle
     * @param y      top edge of the rectangle
     * @param width  width of the rectangle in pixels
     * @param height height of the rectangle in pixels
     * @param color  ARGB border color ({@code 0xAARRGGBB})
     */
    public void outline(int x, int y, int width, int height, int color) {
        /*? if >=26 {*/
        graphics.outline(x, y, width, height, color);
        /*?} else if =1.21.11 { */
        //graphics.renderOutline(x, y, width, height, color);
        /*?} else if <1.21.11 && >=1.21.9 { */
        /*/^for some reason in version 1.21.9 and 1.21.10 renderOutline does not exist?!^/
        this.hLine(x, x + width - 1, y, color); // top
        this.hLine(x, x + width - 1, y + height - 1, color); // bottom
        this.vLine(x, y, y + height - 1, color); //left
        this.vLine(x + width - 1, y, y + height - 1, color); //right
        *//*?} else if >=1.20 { */
        //graphics.renderOutline(x, y, width, height, color);
        /*?} else if >=1.19.4 { */
        //GuiComponent.renderOutline(poseStack, x, y, width, height, color);
        /*?} else {*/
        /*this.hLine(x, x + width - 1, y, color); // top
        this.hLine(x, x + width - 1, y + height - 1, color); // bottom
        this.vLine(x, y, y + height - 1, color); //left
        this.vLine(x + width - 1, y, y + height - 1, color); //right
        *//*?} */
    }

    public void outline(LayoutElement element, int color) {
        outline(element.getX(), element.getY(), element.getWidth(), element.getHeight(), color);
    }


    // ── Text ──────────────────────────────────────────────────────────────────

    /**
     * Draws a plain string with a drop shadow.
     *
     * @param font  the font to render with
     * @param text  the string to draw
     * @param x     left edge of the text
     * @param y     top edge of the text
     * @param color ARGB text color ({@code 0xAARRGGBB})
     */
    public void drawString(Font font, String text, int x, int y, int color) {
        drawString(font, text, x, y, color, true);
    }

    /**
     * @param shadow whether to draw a drop shadow behind the text
     */
    public void drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        /*? if >=26 {*/
        graphics.text(font, text, x, y, color, shadow);
        /*?} else if >=1.20 { */
        //graphics.drawString(font, text, x, y, color, shadow);
        /*?} else { */
        /*if (shadow) font.drawShadow(poseStack, text, x, y, color);
        else font.draw(poseStack, text, x, y, color);
        *//*?} */
    }

    /**
     * Draws a {@link Component} (formatted text) with a drop shadow.
     *
     * @param font  the font to render with
     * @param text  the component to draw
     * @param x     left edge of the text
     * @param y     top edge of the text
     * @param color ARGB text color ({@code 0xAARRGGBB})
     */
    public void drawString(Font font, Component text, int x, int y, int color) {
        drawString(font, text, x, y, color, true);
    }

    public void drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        /*? if >=26 {*/
        graphics.text(font, text, x, y, color, shadow);
        /*?} else if >=1.20 { */
        //graphics.drawString(font, text, x, y, color, shadow);
        /*?} else { */
        /*if (shadow) font.drawShadow(poseStack, text, x, y, color);
        else font.draw(poseStack, text, x, y, color);
        *//*?} */
    }

    /**
     * Draws a {@link FormattedCharSequence} (a pre-shaped, bidi-ordered run of text)
     * with a drop shadow. Use this overload when rendering text returned by
     * {@link Font#split} or tooltip formatting.
     *
     * @param font  the font to render with
     * @param text  the formatted sequence to draw
     * @param x     left edge of the text
     * @param y     top edge of the text
     * @param color ARGB text color ({@code 0xAARRGGBB})
     */
    public void drawString(Font font, FormattedCharSequence text, int x, int y, int color) {
        drawString(font, text, x, y, color, true);
    }

    public void drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        /*? if >=26 {*/
        graphics.text(font, text, x, y, color, shadow);
        /*?} else if >=1.20 { */
        //graphics.drawString(font, text, x, y, color, shadow);
        /*?} else { */
        /*if (shadow) font.drawShadow(poseStack, text, x, y, color);
        else font.draw(poseStack, text, x, y, color);
        *//*?} */
    }

    /**
     * Draws a plain string horizontally centered on {@code x}, with a drop shadow.
     *
     * @param font  the font to render with
     * @param text  the string to draw
     * @param x     the horizontal center point of the text
     * @param y     top edge of the text
     * @param color ARGB text color ({@code 0xAARRGGBB})
     */
    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        drawCenteredString(font, text, x, y, color, true);
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color, boolean shadow) {
        drawString(font, text, x - font.width(text) / 2, y, color, shadow);
    }

    /**
     * Draws a {@link Component} horizontally centered on {@code x}, with a drop shadow.
     *
     * @param font  the font to render with
     * @param text  the component to draw
     * @param x     the horizontal center point of the text
     * @param y     top edge of the text
     * @param color ARGB text color ({@code 0xAARRGGBB})
     */
    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        drawCenteredString(font, text, x, y, color, true);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color, boolean shadow) {
        drawString(font, text, x - font.width(text) / 2, y, color, shadow);
    }

    /**
     * Draws a {@link FormattedCharSequence} horizontally centered on {@code x},
     * with a drop shadow.
     *
     * @param font  the font to render with
     * @param text  the formatted sequence to draw
     * @param x     the horizontal center point of the text
     * @param y     top edge of the text
     * @param color ARGB text color ({@code 0xAARRGGBB})
     */
    public void drawCenteredString(Font font, FormattedCharSequence text, int x, int y, int color) {
        drawCenteredString(font, text, x, y, color, true);
    }

    public void drawCenteredString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        drawString(font, text, x - font.width(text) / 2, y, color, shadow);
    }

    private void drawScrollingStringInternal(Font font, Component text, int left, int top, int right, int bottom, boolean scrollActive, int color, boolean shadow) {
        int textWidth = font.width(text);
        int y = (top + bottom - 8) / 2;
        int availableWidth = right - left;

        if (textWidth <= availableWidth) {
            drawString(font, text, left, y, color, shadow);
            return;
        }

        if (!scrollActive) {
            enableScissor(left, top, right - left, bottom - top);
            drawString(font, text, left, y, color, shadow);
            disableScissor();
            return;
        }

        int overflow = textWidth - availableWidth;
        double time = Util.getMillis() / 1000.0;
        double cycle = Math.max(overflow * 0.5, 3.0);
        double phase = Math.sin(Math.PI / 2.0 * Math.cos(Math.PI * 2.0 * time / cycle)) / 2.0 + 0.5;
        int scrollX = (int) Mth.lerp(phase, 0.0, (double) overflow);

        enableScissor(left, top, right - left, bottom - top);
        drawString(font, text, left - scrollX, y, color, shadow);
        disableScissor();
    }

    /**
     * Top-left anchored, width-limited variant of {@link #drawString(Font, Component, int, int, int)} -
     * scrolls back and forth whenever the text is too wide to fit. For a version that only
     * animates conditionally (e.g. while hovered), use the overload taking {@code scrollActive}.
     *
     * @param font  the font to measure and render with
     * @param text  the text to draw
     * @param x     left edge of the available area
     * @param y     top edge of the text
     * @param width width of the available area in pixels
     * @param color ARGB text color
     */
    public void drawScrollingString(Font font, Component text, int x, int y, int width, int color) {
        drawScrollingString(font, text, x, y, width, true, color, true);
    }
    public void drawScrollingString(Font font, Component text, int x, int y, int width, int color, boolean shadow) {
        drawScrollingString(font, text, x, y, width, true, color, shadow);
    }

    public void drawScrollingString(Font font, String text, int x, int y, int width, int color) {
        drawScrollingString(font, Component.literal(text), x, y, width, color);
    }
    public void drawScrollingString(Font font, String text, int x, int y, int width, int color, boolean shadow) {
        drawScrollingString(font, Component.literal(text), x, y, width, color, shadow);
    }

    /**
     * Same as {@link #drawScrollingString(Font, Component, int, int, int, int)}, but only animates
     * while {@code scrollActive} is true - otherwise the text is drawn statically from its start,
     * clipped to the available width. Intended for list rows that should only scroll while
     * hovered or selected, rather than every row animating at once.
     *
     * @param font         the font to measure and render with
     * @param text         the text to draw
     * @param x            left edge of the available area
     * @param y            top edge of the text
     * @param width        width of the available area in pixels
     * @param scrollActive whether to animate; when false, shows a static, clipped start of the text
     * @param color        ARGB text color
     */
    public void drawScrollingString(Font font, Component text, int x, int y, int width, boolean scrollActive, int color) {
        drawScrollingString(font, text, x, y, width, scrollActive, color, true);
    }
    public void drawScrollingString(Font font, Component text, int x, int y, int width, boolean scrollActive, int color, boolean shadow) {
        drawScrollingStringInternal(font, text, x, y, x + width, y + 8, scrollActive, color, shadow);
    }
    public void drawScrollingString(Font font, String text, int x, int y, int width, boolean scrollActive, int color) {
        drawScrollingString(font, Component.literal(text), x, y, width, scrollActive, color);
    }
    public void drawScrollingString(Font font, String text, int x, int y, int width, boolean scrollActive, int color, boolean shadow) {
        drawScrollingString(font, Component.literal(text), x, y, width, scrollActive, color, shadow);
    }

    /**
     * Same as {@link #drawScrollingString(Font, Component, int, int, int, int)}, but vertically
     * centers the text within a row taller than the normal 8px line - e.g. a list entry with
     * padding above and below its text.
     *
     * @param font   the font to measure and render with
     * @param text   the text to draw
     * @param x      left edge of the available area
     * @param y      top edge of the row
     * @param width  width of the available area in pixels
     * @param height height of the row; the text is centered within it
     * @param color  ARGB text color
     */
    public void drawScrollingString(Font font, Component text, int x, int y, int width, int height, int color) {
        drawScrollingString(font, text, x, y, width, height, true, color, true);
    }
    public void drawScrollingString(Font font, Component text, int x, int y, int width, int height, int color, boolean shadow) {
        drawScrollingString(font, text, x, y, width, height, true, color, shadow);
    }
    public void drawScrollingString(Font font, String text, int x, int y, int width, int height, int color) {
        drawScrollingString(font, Component.literal(text), x, y, width, height, color);
    }
    public void drawScrollingString(Font font, String text, int x, int y, int width, int height, int color, boolean shadow) {
        drawScrollingString(font, Component.literal(text), x, y, width, height, color, shadow);
    }

    /**
     * Combines the height-centering of {@link #drawScrollingString(Font, Component, int, int, int, int, int)}
     * with the conditional animation of {@link #drawScrollingString(Font, Component, int, int, int, boolean, int)}.
     */
    public void drawScrollingString(Font font, Component text, int x, int y, int width, int height, boolean scrollActive, int color) {
        drawScrollingString(font, text, x, y, width, height, scrollActive, color, true);
    }
    public void drawScrollingString(Font font, Component text, int x, int y, int width, int height, boolean scrollActive, int color, boolean shadow) {
        drawScrollingStringInternal(font, text, x, y, x + width, y + height, scrollActive, color, shadow);
    }
    public void drawScrollingString(Font font, String text, int x, int y, int width, int height, boolean scrollActive, int color) {
        drawScrollingString(font, Component.literal(text), x, y, width, height, scrollActive, color);
    }
    public void drawScrollingString(Font font, String text, int x, int y, int width, int height, boolean scrollActive, int color, boolean shadow) {
        drawScrollingString(font, Component.literal(text), x, y, width, height, scrollActive, color, shadow);
    }


    // --- Pose manipulation

    public void pushPose() {
        /*? if >=1.21.6 { */
        graphics.pose().pushMatrix();
        /*?} else if >=1.20 { */
        //graphics.pose().pushPose();
        /*?} else { */
        //poseStack.pushPose();
        /*?} */
    }

    public void popPose() {
        /*? if >=1.21.6 { */
        graphics.pose().popMatrix();
        /*?} else if >=1.20 { */
        //graphics.pose().popPose();
        /*?} else { */
        //poseStack.popPose();
        /*?} */
    }

    /**
     * Translates the current pose.
     * In newer versions (1.21.6+), z translation has limited effect in GUI.
     */
    public void translate(float x, float y, float z) {
        /*? if >=1.21.6 { */
        graphics.pose().translate(x, y);
        /*?} else if >=1.20 { */
        //graphics.pose().translate(x, y, z);
        /*?} else { */
        //poseStack.translate(x, y, z);
        /*?} */
    }

    /**
     * Translates the current pose.
     */
    public void translate(float x, float y) {
        translate(x, y, 0);
    }

    /**
     * Scales the current pose.
     * In newer versions (1.21.6+), z scaling has limited effect in GUI.
     */
    public void scale(float x, float y, float z) {
        /*? if >=1.21.6 { */
        graphics.pose().scale(x, y);
        /*?} else if >=1.20 { */
        //graphics.pose().scale(x, y, z);
        /*?} else { */
        //poseStack.scale(x, y, z);
        /*?} */
    }

    /**
     * Scales the current pose.
     */
    public void scale(float x, float y) {
        scale(x, y, 1);
    }

    /*? if <1.19.4 { */
    /*/^*
     * Copies the current accumulated poseStack transform onto RenderSystem's separate
     * model-view stack, for the handful of legacy (pre-1.19.4) vanilla render calls that
     * read that stack instead of the poseStack we're given - item rendering being the
     * only one identified so far. Must be paired with restoreModelView() afterward.
     ^/
    private void syncModelViewToPoseStack() {
        RenderSystem.getModelViewStack().pushPose();
        /^~ if >= 1.19.3 'multiply' -> 'mul'  ^/
        RenderSystem.getModelViewStack().last().pose().mul(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
    }

    private void restoreModelView() {
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }
*//*?} */

    // --- Scissor

    /**
     * Restricts subsequent rendering to the given rectangular region.
     * Anything drawn outside this region is clipped and not visible.
     *
     * <p>Scissor calls are stacked: each call to {@code enableScissor} must be
     * matched by exactly one call to {@link #disableScissor()}. The effective
     * clip region is the intersection of all active scissor rectangles.</p>
     *
     * <p>Note: parameters are {@code (x, y, width, height)}, <em>not</em> two
     * corner coordinates. The conversion to corner coordinates is handled
     * internally.</p>
     *
     * @param x      left edge of the scissor rectangle
     * @param y      top edge of the scissor rectangle
     * @param width  width of the scissor rectangle in pixels
     * @param height height of the scissor rectangle in pixels
     * @see #disableScissor()
     */
    public void enableScissor(int x, int y, int width, int height) {
        /*? if >=1.20 {*/
        graphics.enableScissor(x, y, x + width, y + height);
        /*?} else if >=1.19.1 {*/
        //GuiComponent.enableScissor(x, y, x + width, y + height);
        /*?} else {*/
        //ScissorStack.push(x, y, width, height);
        /*?}*/
    }

    /**
     * Removes the innermost scissor region previously set by {@link #enableScissor}.
     * Rendering after this call is clipped to the next outer scissor region,
     * or not clipped at all if no other scissor is active.
     *
     * @see #enableScissor(int, int, int, int)
     */
    public void disableScissor() {
        /*? if >=1.20 {*/
        graphics.disableScissor();
        /*?} else if >=1.19.1 {*/
        //GuiComponent.disableScissor();
        /*?} else {*/
        //ScissorStack.pop();
        /*?}*/
    }


    // --- Textures

    /**
     * Draws a {@link GenericTexture} at its natural size, as defined by
     * {@link GenericTexture#regionWidth()} and {@link GenericTexture#regionHeight()}.
     *
     * <p>On Minecraft 1.20.2 and above the sprite's {@code .mcmeta} scaling
     * metadata is respected automatically (stretch, tile, or nine-slice).</p>
     *
     * @param texture the texture to draw
     * @param x       left edge of the rendered image
     * @param y       top edge of the rendered image
     * @see #drawTexture(GenericTexture, int, int, int, int)
     */
    public void drawTexture(GenericTexture texture, int x, int y) {
        if (texture instanceof GenericSprite sprite) {
            drawSprite(sprite, x, y);
        } else {
            drawTexture(texture, x, y, texture.regionWidth(), texture.regionHeight());
        }
    }

    /**
     * Draws a {@link GenericTexture} scaled to the given on-screen dimensions.
     * The source region defined by the texture is stretched or shrunk to fill
     * the requested {@code width} &times; {@code height} area.
     *
     * @param texture the texture to draw
     * @param x       left edge of the rendered image
     * @param y       top edge of the rendered image
     * @param width   desired on-screen width in pixels
     * @param height  desired on-screen height in pixels
     * @see #drawTexture(GenericTexture, int, int)
     */
    public void drawTexture(GenericTexture texture, int x, int y, int width, int height) {
        if (texture instanceof GenericSprite sprite) {
            drawSprite(sprite, x, y, width, height);
        } else {
            /*? if <1.20 */
            //RenderSystem.setShaderTexture(0, texture.getSprite());
            internalBlit(texture, x, y, width, height);
        }
    }

    public void drawTextureRepeating(GenericTexture texture, int x, int y, int width, int height) {
        if (texture instanceof GenericSprite sprite) {
            drawSprite(sprite, x, y, width, height);
        } else {
            /*? if <1.20 */
            //RenderSystem.setShaderTexture(0, texture.getSprite());
            drawRepeating(texture, x, y, width, height, texture.atlasX(), texture.atlasY(), texture.regionWidth(), texture.regionHeight());
        }
    }

    /**
     * Draws a {@link GenericSprite} at its natural size, as defined by
     * {@link GenericSprite#regionWidth()} and {@link GenericSprite#regionHeight()}.
     *
     * <p>On Minecraft 1.20.2 and above the sprite's {@code .mcmeta} scaling
     * metadata is respected automatically (stretch, tile, or nine-slice).</p>
     *
     * @param texture the texture to draw
     * @param x       left edge of the rendered image
     * @param y       top edge of the rendered image
     * @see #drawSprite(GenericSprite, int, int, int, int)
     */
    public void drawSprite(GenericSprite texture, int x, int y) {
        drawSprite(texture, x, y, texture.regionWidth(), texture.regionHeight());
    }

    /**
     * Draws a {@link GenericSprite} scaled to the given on-screen dimensions.
     * The source region defined by the texture is stretched or shrunk to fill
     * the requested {@code width} &times; {@code height} area.
     *
     * <p>On Minecraft 1.20.2 and above the sprite's {@code .mcmeta} scaling
     * metadata is respected automatically, so a sprite with {@code "type": "nine_slice"}
     * metadata will be nine-sliced rather than uniformly stretched. <a href="https://minecraft.wiki/w/Resource_pack#GUI">Wiki article</a></p>
     *
     * @param texture the texture to draw
     * @param x       left edge of the rendered image
     * @param y       top edge of the rendered image
     * @param width   desired on-screen width in pixels
     * @param height  desired on-screen height in pixels
     * @see #drawSprite(GenericSprite, int, int)
     */
    public void drawSprite(GenericSprite texture, int x, int y, int width, int height) {
        /*? if >=1.21.6 {*/
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture.getSprite(), x, y, width, height);
        /*?} else if >=1.21.2 { */
        //graphics.blitSprite(RenderType::guiTextured, texture.getSprite(), x, y, width, height);
        /*?} else if >=1.20.2 { */
        //graphics.blitSprite(texture.getSprite(), x, y, width, height);
        /*?} else { */
         /*if (texture instanceof TiledSprite tiled) {
            //? if < 1.20
            //RenderSystem.setShaderTexture(0, texture.getSprite());
            drawRepeating(tiled, x, y, width, height, tiled.atlasX(), tiled.atlasY(),  tiled.regionWidth(), tiled.regionHeight());
        } else if (texture instanceof NineSlicedSprite nineSliced) {
            drawNineSliced(nineSliced, x, y, width, height);
        } else {
             //? if < 1.20
             //RenderSystem.setShaderTexture(0, texture.getSprite());
             internalBlit(texture, x, y, width, height);
         }
        *//*?} */
    }

    /**
     * Draws a nine-sliced (nine-patch) rendering of a {@link NineSlicedSprite}
     * with individually sized borders on each side.
     *
     * <p>The rendered area is divided into a 3&times;3 grid: four fixed-size corners,
     * four edges that scale along one axis, and a center that scales along both axes.</p>
     *
     * <p><strong>Minecraft 1.20.2 and above:</strong> the border parameters are
     * <em>ignored</em>. The sprite's {@code .mcmeta} file must declare
     * {@code "type": "nine_slice"} with the desired border sizes. The call is
     * forwarded to {@link #drawSprite(GenericSprite, int, int, int, int)}, which
     * lets the sprite system apply the metadata-driven nine-slice automatically.
     * Custom textures without a suitable {@code .mcmeta} will be stretched instead.</p>
     *
     * <p><strong>Minecraft below 1.20.2:</strong> the border parameters control
     * the corner sizes in pixels. The edges and center are tiled to fill the
     * remaining space.</p>
     *
     * @param texture the texture to draw
     * @param x       left edge of the rendered area
     * @param y       top edge of the rendered area
     * @param width   total width of the rendered area in pixels
     * @param height  total height of the rendered area in pixels
     */
    public void drawNineSliced(NineSlicedSprite texture, int x, int y, int width, int height) {
        /*? if >=1.20.2 {*/
        drawSprite(texture, x, y, width, height);
        /*?} else { */
        /*//? if < 1.20
        //RenderSystem.setShaderTexture(0, texture.getSprite());
        internalDrawNineSliced(texture, x, y, width, height);
         *//*?} */
    }

    //own version to work with non-square textures
    /*? if <1.20.2 { */
    /*private void internalDrawNineSliced(NineSlicedSprite texture, int x, int y, int width, int height) {
        //ensure no overlapping
        int left = Math.min(texture.getLeft(), width / 2);
        int right = Math.min(texture.getRight(), width / 2);
        int top = Math.min(texture.getTop(), height / 2);
        int bottom = Math.min(texture.getBottom(), height / 2);

        if (width == texture.regionWidth() && height == texture.regionHeight()) { //nine spliced texture same size as sprite region -> draw as is
            internalBlit(texture, x, y);
        } else if (width == texture.regionWidth()) { //nine spliced texture same width as sprite region -> draw top as is -> draw whole width repeating -> draw bottom as is
            internalBlit(texture, x, y, texture.atlasX(), texture.atlasY(), width, top, width, top); // draw top
            drawRepeating(texture, x, y + top, width, height - top - bottom, texture.atlasX(), texture.atlasY() + top, texture.regionWidth(), texture.regionHeight() - top - bottom);
            internalBlit(texture, x, y + height - bottom, texture.atlasX(), texture.atlasY() + texture.regionHeight() - bottom, width, bottom, width, bottom); // draw bottom
        } else if (height == texture.regionHeight()) {
            internalBlit(texture, x, y, texture.atlasX(), texture.atlasY(), left, height, left, height); // draw left
            drawRepeating(texture, x + left, y, width - left - right, height, texture.atlasX() + left, texture.atlasY(), texture.regionWidth() - left - right, texture.regionHeight());
            internalBlit(texture, x + width - right, y, texture.atlasX() + texture.regionWidth() - right, texture.atlasY(), right, height, right, height); // draw right
        } else {
            //corners
            internalBlit(texture, x, y, texture.atlasX(), texture.atlasY(), left, top, left, top); //top left
            internalBlit(texture, x + width - right, y, texture.atlasX() + texture.regionWidth() - right, texture.atlasY(), right, top, right, top); //top right
            internalBlit(texture, x, y + height - bottom, texture.atlasX(), texture.atlasY() + texture.regionHeight() - bottom, right, bottom, right, bottom); //bottom left
            internalBlit(texture, x + width - right, y + height - bottom, texture.atlasX() + texture.regionWidth() - right, texture.atlasY() + texture.regionHeight() - bottom, left, bottom, left, bottom); //bottom right
            //straights
            drawRepeating(texture, x + left, y, width - left - right, top, texture.atlasX() + left, texture.atlasY(), texture.regionWidth() - left - right, top); //top
            drawRepeating(texture, x + left, y + height - bottom, width - left - right, bottom, texture.atlasX() + left, texture.atlasY() + texture.regionHeight() - bottom, texture.regionWidth() - left - right, bottom); //bottom
            drawRepeating(texture, x, y + top, left, height - top - bottom, texture.atlasX(), texture.atlasY() + top, left, texture.regionHeight() - top - bottom); //left
            drawRepeating(texture, x + width - right, y + top, right, height - top - bottom, texture.atlasX() + texture.regionWidth() - right, texture.atlasY() + top, right, texture.regionHeight() - top - bottom); //right
            //center

            if (texture.stretchInner()) {
                internalBlit(texture, x + left, y + top, texture.atlasX() + left, texture.atlasY() + top,width - left - right, height - top - bottom, texture.regionWidth() - left - right, texture.regionHeight() - top - bottom);
            } else {
                drawRepeating(texture, x + left, y + top, width - left - right, height - top - bottom, texture.atlasX() + left, texture.atlasY() + top, texture.regionWidth() - left - right, texture.regionHeight() - top - bottom);
            }
        }
    }
    *//*?} */

    private void drawRepeating(GenericTexture texture, int x, int y, int width, int height, float u, float v, int repeatWidth, int repeatHeight) {
        int currentX = x;

        while (currentX < x + width) {
            int drawWidth = Math.min(repeatWidth, x + width - currentX);
            int currentY = y;
            while (currentY < y + height) {
                int drawHeight = Math.min(repeatHeight, y + height - currentY);
                internalBlit(texture, currentX, currentY,         // where to draw on screen
                        u, v,                       // where to sample from texture
                        drawWidth, drawHeight,      // how big to draw this piece
                        repeatWidth, repeatHeight   // how big are on texture is
                );
                currentY += drawHeight;
            }
            currentX += drawWidth;
        }
    }

    // GenericGraphics

    /**
     * Draws a precise sub-rectangle of a texture — samples the region at
     * ({@code u}, {@code v}) sized {@code regionWidth} x {@code regionHeight} in
     * texture-space, and draws it on screen at ({@code x}, {@code y}) sized
     * {@code width} x {@code height} (stretched if the two sizes differ).
     *
     * <p>Use this to blit an arbitrary piece of a larger texture file (one border
     * segment out of a container texture, say) rather than a {@link GenericTexture}'s
     * own predefined region as a whole — {@link #drawTexture} always draws a
     * texture's own fixed region; this lets the caller pick a different one on the
     * fly from the same underlying image.</p>
     *
     * @param texture      the texture whose underlying image to sample from — only
     *                     its image reference and atlas dimensions are used; its own
     *                     atlasX/atlasY/regionWidth/regionHeight are ignored
     * @param x            left edge to draw at
     * @param y            top edge to draw at
     * @param u            left edge to sample from, in texture-space pixels
     * @param v            top edge to sample from, in texture-space pixels
     * @param width        on-screen width
     * @param height       on-screen height
     * @param regionWidth  width of the sampled region, in texture-space pixels
     * @param regionHeight height of the sampled region, in texture-space pixels
     */
    public void blit(GenericTexture texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight) {
        internalBlit(texture, x, y, u, v, width, height, regionWidth, regionHeight);
    }

    private void internalBlit(GenericTexture texture, int x, int y) {
        internalBlit(texture, x, y, texture.regionWidth(), texture.regionHeight());
    }

    private void internalBlit(GenericTexture texture, int x, int y, int width, int height) {
        internalBlit(texture, x, y, texture.atlasX(), texture.atlasY(), width, height);
    }

    private void internalBlit(GenericTexture texture, int x, int y, float u, float v, int width, int height) {
        internalBlit(texture, x, y, u, v, width, height, texture.regionWidth(), texture.regionHeight());
    }

    /*? if >=1.21.6 { */
    private void internalBlit(GenericTexture texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture.getSprite(), x, y, u, v, width, height, regionWidth, regionHeight, texture.atlasWidth(), texture.atlasHeight());
    }
    /*?} else if >=1.21.2 { */
    /*private void internalBlit(GenericTexture texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight) {
        graphics.blit(RenderType::guiTextured, texture.getSprite(), x, y, u, v, width, height, regionWidth, regionHeight, texture.atlasWidth(), texture.atlasHeight());
    }
    *//*?} else if >=1.20.2 { */
    /*private void internalBlit(GenericTexture texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight) {
        graphics.blit(texture.getSprite(), x, y, width, height, u, v,  regionWidth, regionHeight, texture.atlasWidth(), texture.atlasHeight());
    }
    *//*?} else if >=1.20 { */
    /*private void internalBlit(GenericTexture texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight) {
        graphics.blit(texture.getSprite(), x, y, width, height, u, v, regionWidth, regionHeight, texture.atlasWidth(), texture.atlasHeight());
    }
    *//*?} else { */
    /*private void internalBlit(GenericTexture texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight) {
        //drawTexture(MatrixStack matrices, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight)
        RenderSystem.setShaderTexture(0, texture.getSprite());
        GuiComponent.blit(poseStack, x, y, width, height, u, v, regionWidth, regionHeight, texture.atlasWidth(), texture.atlasHeight());
    }
    *//*?} */

    public void requestCursor(GenericCursors cursor) {
        /*? if >=1.21.9 { */
        graphics.requestCursor(cursor.getVanillaType());
        /*?} */
    }

    public void renderItem(Item item, int x, int y) {
        renderItem(DisplayItemStack.getStackSimple(item), x, y);
    }

    public void renderItem(ItemStack itemStack, int x, int y) {
        /*? if >=26 { */
        graphics.item(itemStack, x, y);
        /*?} else if >=1.20 { */
        //graphics.renderItem(itemStack, x, y);
        /*?} else if >=1.19.4 {*/
        //Minecraft.getInstance().getItemRenderer().renderGuiItem(poseStack, itemStack, x, y);
        /*?} else {*/
        /*syncModelViewToPoseStack();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(itemStack, x, y);
        restoreModelView();
        *//*?} */
    }

    public void renderItemDecorations(Font font, ItemStack itemStack, int x, int y) {
        renderItemDecorations(font, itemStack, x, y, null);
    }

    public void renderItemDecorations(Font font, ItemStack itemStack, int x, int y, @Nullable String countText) {
        /*? if >=26 { */
        graphics.itemDecorations(font, itemStack, x, y, countText);
        /*?} else if >=1.20 { */
        //graphics.renderItemDecorations(font, itemStack, x, y, countText);
        /*?} else if >=1.19.4 {*/
        //Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(poseStack, font, itemStack, x, y, countText);
        /*?} else {*/
        /*syncModelViewToPoseStack();
        Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, itemStack, x, y, countText);
        restoreModelView();
         *//*?} */
    }

    public void renderDecoratedItem(Font font, Item item, int x, int y) {
        renderDecoratedItem(font, DisplayItemStack.getStackSimple(item), x, y);
    }

    public void renderDecoratedItem(Font font, Item item, int x, int y, String countText) {
        renderDecoratedItem(font, DisplayItemStack.getStackSimple(item), x, y, countText);
    }

    public void renderDecoratedItem(Font font, ItemStack itemStack, int x, int y) {
        renderDecoratedItem(font, itemStack, x, y, null);
    }

    public void renderDecoratedItem(Font font, ItemStack itemStack, int x, int y, String countText) {
        renderItem(itemStack, x, y);
        renderItemDecorations(font, itemStack, x, y, countText);
    }

    /**
     * Draws an {@code ItemStack} icon scaled to a square of the given size (native item
     * rendering is always 16&times;16). Handles the push/translate/scale/pop pose
     * bookkeeping internally - including the legacy pre-1.19.4 model-view sync already
     * built into {@link #renderItem(ItemStack, int, int)} - so callers never need to
     * touch the pose stack themselves.
     *
     * @param itemStack the item to draw
     * @param x         left edge of the icon
     * @param y         top edge of the icon
     * @param size      desired on-screen width/height in pixels
     */
    public void renderScaledItem(ItemStack itemStack, int x, int y, int size) {
        float scale = size / 16f;
        pushPose();
        translate(x, y);
        scale(scale, scale);
        renderItem(itemStack, 0, 0);
        popPose();
    }


}
