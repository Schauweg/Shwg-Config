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
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/*? if >=1.19.3 {*/
import org.joml.Quaternionf;
import org.joml.Vector3f;
/*?} else {*/
/*import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
*//*?}*/

/*? if >=1.21.11 {*/
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.entity.animal.cow.CowVariants;
import net.minecraft.world.entity.animal.pig.PigVariants;
/*?} else if >=1.21.5 {*/
/*import net.minecraft.world.entity.animal.ChickenVariants;
import net.minecraft.world.entity.animal.CowVariants;
import net.minecraft.world.entity.animal.PigVariants;
*//*?}*/

/*? if >=1.21.2 {*/
import dev.shwg.shwgconfig.mixin.EntityRenderDispatcherAccessor;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.entity.state.*;
/*?}*/

/*? if <1.21.2 {*/
/*import dev.shwg.shwgconfig.mixin.EntityAccessor;
import net.minecraft.world.entity.animal.AbstractFish;
*//*?}*/

/*? if >=1.21.5 {*/
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
/*?}*/

/*? if >=1.21.2 && <1.21.6 {*/
/*import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
*//*?} else if >=1.19.4 && <1.21.2 {*/
//import net.minecraft.client.gui.screens.inventory.InventoryScreen;
/*?} else if <1.19.4 {*/
/*import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
*//*?}*/

/*? if >=1.21.11 {*/
import net.minecraft.util.Util;
/*?} else {*/
//import net.minecraft.Util;
/*?}*/

/*? if >=1.21.6 {*/
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2fStack;
/*?} else {*/
//import com.mojang.blaze3d.vertex.PoseStack;
/*?}*/

/*? if <=1.21.5 && >=1.21.2 {*/
//import net.minecraft.client.renderer.RenderType;
/*?}*/

/*? if >=1.20 {*/
import net.minecraft.client.gui.GuiGraphics;
/*?} else {*/
/*import net.minecraft.client.gui.GuiComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.Screen;
*//*?}*/

/*? if <1.19.4 { */
//import dev.shwg.shwgconfig.render.scissor.ScissorStack;
/*?} */

/**
 * Cross-version abstraction layer over Minecraft's GUI rendering API.
 *
 * <p>Wraps the version-specific rendering context - {@code GuiGraphics} for
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
    private final GuiGraphics graphics;

    /**
     * Creates a {@code GenericGraphics} instance backed by the given {@code GuiGraphics}.
     * This constructor is used for Minecraft 1.20 and above.
     *
     * @param guiGraphics the native rendering context provided by the current render frame
     **/
    public GenericGraphics(@NotNull GuiGraphics guiGraphics) {
        this.graphics = guiGraphics;
        this.deferredElements = new ArrayList<>();
    }

    /**
     * Returns the underlying {@code GuiGraphics} instance (Minecraft 1.20+).
     *
     * <p>Prefer the wrapper methods on this class over calling the native API directly,
     * as direct usage bypasses cross-version compatibility.</p>
     *
     * @return the native rendering context
     **/
    public GuiGraphics getGraphics() {
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
        Font font = Minecraft.getInstance().font;
        List<Component> lines = tooltip.getLines(font);

        /*? if >=1.21.6 { */
        graphics.setTooltipForNextFrame(font, lines, image, mouseX, mouseY);
        /*?} else if >=1.20 { */
        //graphics.renderTooltip(font, lines, image, mouseX, mouseY);
        /*?} else { */
        /*Screen screen = Minecraft.getInstance().screen;
        if (screen != null) {
            screen.renderTooltip(poseStack, lines, image, mouseX, mouseY);
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

    // --- Fill

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


    // --- Lines

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
        //graphics.horizontalLine(x0, x1, y, color);
        /*?} else if >=1.20 { */
        graphics.hLine(x0, x1, y, color);
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
        //graphics.verticalLine(x, y0, y1, color);
        /*?} else if >=1.20 { */
        graphics.vLine(x, y0, y1, color);
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
        //graphics.outline(x, y, width, height, color);
        /*?} else if =1.21.11 { */
        graphics.renderOutline(x, y, width, height, color);
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


    // --- Text

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
        /*? if <=1.21.5 {*/
        //if ((color & 0xFC000000) == 0) return; // old vanilla forces alpha 0-3 to opaque, newer versions draw nothing
        /*?}*/
        /*? if >=26 {*/
        //graphics.text(font, text, x, y, color, shadow);
        /*?} else if >=1.20 { */
        graphics.drawString(font, text, x, y, color, shadow);
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
        /*? if <=1.21.5 {*/
        //if ((color & 0xFC000000) == 0) return; // old vanilla forces alpha 0-3 to opaque, newer versions draw nothing
        /*?}*/
        /*? if >=26 {*/
        //graphics.text(font, text, x, y, color, shadow);
        /*?} else if >=1.20 { */
        graphics.drawString(font, text, x, y, color, shadow);
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
        /*? if <=1.21.5 {*/
        //if ((color & 0xFC000000) == 0) return; // old vanilla forces alpha 0-3 to opaque, newer versions draw nothing
        /*?}*/
        /*? if >=26 {*/
        //graphics.text(font, text, x, y, color, shadow);
        /*?} else if >=1.20 { */
        graphics.drawString(font, text, x, y, color, shadow);
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
            enableScissor(left, top, right, bottom);
            drawString(font, text, left, y, color, shadow);
            disableScissor();
            return;
        }

        int overflow = textWidth - availableWidth;
        double time = Util.getMillis() / 1000.0;
        double cycle = Math.max(overflow * 0.5, 3.0);
        double phase = Math.sin(Math.PI / 2.0 * Math.cos(Math.PI * 2.0 * time / cycle)) / 2.0 + 0.5;
        int scrollX = (int) Mth.lerp(phase, 0.0, (double) overflow);

        enableScissor(left, top, right, bottom);
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
     * Returns the current transformation stack of this rendering context, so callers
     * can apply transforms (translate, scale, rotate) that the wrapper methods don't cover.
     *
     * <p><strong>The return type depends on the Minecraft version:</strong>
     * {@code Matrix3x2fStack} (2D) on 1.21.6+, {@code PoseStack} on all older versions.
     * Callers using the result directly need their own version conditionals.
     * Always pair changes with {@link #pushPose()} / {@link #popPose()}.</p>
     */
    /*? if >=1.21.6 { */
    public Matrix3x2fStack getPose() {
        return graphics.pose();
    }
    /*?} else if >=1.20 { */
    /*public PoseStack getPose() {
        return graphics.pose();
    }
    *//*?} else { */
    /*public PoseStack getPose() {
        return poseStack;
    }
    *//*?} */

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

    /**
     * Rotates the current pose around the Z axis (i.e. in the screen plane).
     *
     * @param radians rotation angle in radians, positive = clockwise on screen
     */
    public void rotate(float radians) {
        /*? if >=1.21.6 { */
        graphics.pose().rotate(radians);
        /*?} else if >=1.20 { */
        //graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation(radians));
        /*?} else if >=1.19.3 { */
        //poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(radians));
        /*?} else { */
        //poseStack.mulPose(com.mojang.math.Vector3f.ZP.rotation(radians));
        /*?} */
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

    // --- Entities

    /**
     * Draws an entity inside the box {@code (x, y, width, height)}, scaled to fit it.
     *
     * @see #drawEntity(LivingEntity, int, int, int, int, float, float)
     */
    public void drawEntityWH(LivingEntity entity, int x, int y, int width, int height, float yaw, float pitch) {
        drawEntity(entity, x, y, x + width, y + height, yaw, pitch);
    }

    /**
     * Draws a default-looking entity of the given type inside the box {@code (x, y, width, height)}.
     *
     * @see #drawEntity(EntityType, int, int, int, int, float, float)
     */
    public void drawEntityWH(EntityType<?> type, int x, int y, int width, int height, float yaw, float pitch) {
        drawEntity(type, x, y, x + width, y + height, yaw, pitch);
    }

    /**
     * Draws an entity inside the box from {@code (x0, y0)} to {@code (x1, y1)}, centered
     * and scaled to fit it. Anything outside the box is cut off. The box follows the current
     * pose (translate/scale) on every version, like every other draw call.
     *
     * @param yaw   turn around the vertical axis in degrees; 0 faces the viewer, positive turns clockwise
     * @param pitch tilt towards the viewer in degrees; positive looks down from above
     */
    public void drawEntity(LivingEntity entity, int x0, int y0, int x1, int y1, float yaw, float pitch) {
        if (x1 <= x0 || y1 <= y0) return; // an empty box can't be drawn (and crashes the texture creation on 1.21.6+)
        /*? if >=1.21.2 {*/
        EntityRenderer<?, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        submitEntityState(renderer, extractFreshState(renderer, entity), x0, y0, x1, y1, yaw, pitch);
        /*?} else {*/
        /*float bodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float headRotO = entity.yHeadRotO;
        float headRot = entity.yHeadRot;
        entity.yBodyRot = 180.0F + yaw;
        entity.setYRot(180.0F + yaw);
        entity.setXRot(0.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        EntityAccessor water = (EntityAccessor) entity;
        boolean wasInWater = water.shwgconfig$getWasTouchingWater();
        if (entity instanceof AbstractFish) {
            water.shwgconfig$setWasTouchingWater(true);
        }

        ModelBounds bounds = ModelBounds.hitbox(entity.getBbWidth(), entity.getBbHeight());
        if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer<?, ?> livingRenderer) {
            bounds = bounds.union(ModelBounds.of(livingRenderer, entity));
        }
        ModelBounds.ScreenBounds screen = bounds.project(yaw, pitch);
        float scale = fitScale(x1 - x0, y1 - y0, screen.width(), screen.height());

        enableScissor(x0, y0, x1, y1);
        renderEntityInBox(entity, x0, y0, x1, y1, scale, pitch, -screen.centerX(), -screen.centerY());
        disableScissor();

        entity.yBodyRot = bodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = headRotO;
        entity.yHeadRot = headRot;
        water.shwgconfig$setWasTouchingWater(wasInWater);
        *//*?}*/
    }

    /**
     * Draws a default-looking entity of the given type inside the box from {@code (x0, y0)}
     * to {@code (x1, y1)}, without needing a world. Draws nothing for types without a
     * renderer (e.g. players). The box follows the current pose on every version.
     *
     * @param yaw   turn around the vertical axis in degrees; 0 faces the viewer, positive turns clockwise
     * @param pitch tilt towards the viewer in degrees; positive looks down from above
     */
    public void drawEntity(EntityType<?> type, int x0, int y0, int x1, int y1, float yaw, float pitch) {
        if (x1 <= x0 || y1 <= y0) return;
        /*? if >=1.21.2 {*/
        Map<EntityType<?>, ?> renderers = ((EntityRenderDispatcherAccessor) Minecraft.getInstance().getEntityRenderDispatcher()).shwgconfig$getRenderers();
        if (!(renderers.get(type) instanceof EntityRenderer<?, ?> renderer)) return;

        EntityRenderState state = renderer.createRenderState();
        state.boundingBoxWidth = type.getWidth();
        state.boundingBoxHeight = type.getHeight();
        /*? if >=1.21.6 {*/
        state.entityType = type;
        /*?}*/
        /*? if >=1.21.5 {*/
        applyDefaults(state);
        /*?}*/
        submitEntityState(renderer, state, x0, y0, x1, y1, yaw, pitch);
        /*?} else {*/
        /*// before 1.21.2: a real entity if a world is loaded, otherwise just the model
        LivingEntity worldEntity = EntityModelRenderer.worldEntity(type);
        if (worldEntity != null) {
            drawEntity(worldEntity, x0, y0, x1, y1, yaw, pitch);
        } else {
            EntityModelRenderer.drawModel(this, type, x0, y0, x1, y1, yaw, pitch);
        }
        *//*?}*/
    }

    /*? if >=1.21.2 {*/
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityRenderState extractFreshState(EntityRenderer renderer, Entity entity) {
        EntityRenderState state = renderer.createRenderState();
        renderer.extractRenderState(entity, state, 1.0F);
        return state;
    }

    private void submitEntityState(EntityRenderer<?, ?> renderer, EntityRenderState state, int x0, int y0, int x1, int y1, float yaw, float pitch) {
        /*? if >=1.21.6 {*/
        // The picture-in-picture render takes its box in screen coordinates and ignores the pose,
        // so map the box through it first. The scale below is then computed from the real on-screen size.
        int[] box = mapBoxThroughPose(x0, y0, x1, y1);
        x0 = box[0];
        y0 = box[1];
        x1 = box[2];
        y1 = box[3];
        if (x1 <= x0 || y1 <= y0) return; // scaled down below one pixel
        /*?}*/
        /*? if >=1.21.6 && <1.21.11 {*/
        //state.hitboxesRenderState = null;
        /*?}*/
        /*? if >=1.21.9 {*/
        state.shadowPieces.clear();
        state.outlineColor = 0;
        /*?}*/
        /*? if >=1.21.9 && <26 {*/
        state.lightCoords = 15728880;
        /*?}*/

        ModelBounds bounds = ModelBounds.hitbox(state.boundingBoxWidth, state.boundingBoxHeight);
        float viewYaw = 0.0F;
        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0F + yaw;
            living.yRot = 0.0F;
            living.xRot = 0.0F;
            float entityScale = living.scale > 0.0F ? living.scale : 1.0F;
            living.boundingBoxWidth = living.boundingBoxWidth / entityScale;
            living.boundingBoxHeight = living.boundingBoxHeight / entityScale;
            living.scale = 1.0F;
            if (renderer instanceof CodRenderer || renderer instanceof SalmonRenderer || renderer instanceof TropicalFishRenderer) {
                living.isInWater = true;
            }
            bounds = ModelBounds.hitbox(living.boundingBoxWidth, living.boundingBoxHeight);
            if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer) {
                bounds = bounds.union(ModelBounds.of(livingRenderer, living));
            }
            viewYaw = yaw;
        }

        ModelBounds.ScreenBounds screen = bounds.project(viewYaw, pitch);
        float scale = fitScale(x1 - x0, y1 - y0, screen.width(), screen.height());
        Quaternionf pitchRotation = new Quaternionf().rotateX((float) Math.toRadians(-pitch));
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(pitchRotation);
        Vector3f translation = new Vector3f(-screen.centerX(), -screen.centerY(), 0.0F);
        /*? if >=26 {*/
        //graphics.entity(state, scale, translation, rotation, pitchRotation, x0, y0, x1, y1);
        /*?} else if >=1.21.6 {*/
        graphics.submitEntityRenderState(state, scale, translation, rotation, pitchRotation, x0, y0, x1, y1);
        /*?} else {*/
        //renderStateNow(renderer, state, x0, y0, x1, y1, scale, translation, rotation, pitchRotation);
        /*?}*/
    }

    /*? if >=1.21.6 {*/
    private int[] mapBoxThroughPose(int x0, int y0, int x1, int y1) {
        org.joml.Vector2f a = graphics.pose().transformPosition(x0, y0, new org.joml.Vector2f());
        org.joml.Vector2f b = graphics.pose().transformPosition(x1, y1, new org.joml.Vector2f());
        return new int[]{
                Math.round(Math.min(a.x, b.x)), Math.round(Math.min(a.y, b.y)),
                Math.round(Math.max(a.x, b.x)), Math.round(Math.max(a.y, b.y))};
    }
    /*?}*/

    /*? if <1.21.6 {*/
    /*// 1.21.2 - 1.21.5: the GUI can't take a render state yet, so it's drawn right away,
    // the same way vanilla's inventory does it in these versions
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderStateNow(EntityRenderer<?, ?> renderer, EntityRenderState state, int x0, int y0, int x1, int y1,
                                float scale, Vector3f translation, Quaternionf rotation, Quaternionf pitchRotation) {
        EntityRenderer rawRenderer = renderer; // raw: the state's exact type isn't known here
        enableScissor(x0, y0, x1, y1);
        graphics.pose().pushPose();
        graphics.pose().translate((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, 50.0F);
        graphics.pose().scale(scale, scale, -scale);
        graphics.pose().translate(translation.x, translation.y, translation.z);
        graphics.pose().mulPose(rotation);
        graphics.flush();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.overrideCameraOrientation(pitchRotation.conjugate(new Quaternionf()).rotateY((float) Math.PI));
        graphics.drawSpecial(buffers -> rawRenderer.render(state, graphics.pose(), buffers, 15728880));
        graphics.flush();
        graphics.pose().popPose();
        Lighting.setupFor3DItems();
        disableScissor();
    }
    *//*?}*/

    /*? if >=1.21.5 {*/

    private static HolderLookup.Provider vanillaRegistries;

    private static HolderLookup.Provider registries() {
        if (Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.registryAccess();
        }
        if (vanillaRegistries == null) {
            /*~ if >= 26.3 '.createLookup()' -> '.createWorldLookup()'*/
            vanillaRegistries = VanillaRegistries.createLookup();
        }
        return vanillaRegistries;
    }

    private static <T> T registryValue(ResourceKey<? extends Registry<? extends T>> registry, ResourceKey<T> key) {
        return registries().lookupOrThrow(registry).getOrThrow(key).value();
    }

    private static void applyDefaults(EntityRenderState state) {
        if (state instanceof PigRenderState pig) {
            pig.variant = registryValue(Registries.PIG_VARIANT, PigVariants.DEFAULT);
        } else if (state instanceof CowRenderState cow) {
            cow.variant = registryValue(Registries.COW_VARIANT, CowVariants.DEFAULT);
        } else if (state instanceof ChickenRenderState chicken) {
            chicken.variant = registryValue(Registries.CHICKEN_VARIANT, ChickenVariants.DEFAULT);
        }
    }
    /*?}*/
    /*?}*/

    /*? if >=1.20.6 && <1.21.2 {*/
    /*private void renderEntityInBox(LivingEntity entity, int x0, int y0, int x1, int y1, float scale, float pitch, float offsetX, float offsetY) {
        Quaternionf pitchRotation = new Quaternionf().rotateX((float) Math.toRadians(-pitch));
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(pitchRotation);
        Vector3f translation = new Vector3f(offsetX, offsetY, 0.0F);
        InventoryScreen.renderEntityInInventory(graphics, (x0 + x1) / 2.0F, (y0 + y1) / 2.0F, scale, translation, rotation, pitchRotation, entity);
    }
    *//*?} else if >=1.20.2 && <1.20.6 {*/
    /*private void renderEntityInBox(LivingEntity entity, int x0, int y0, int x1, int y1, float scale, float pitch, float offsetX, float offsetY) {
        Quaternionf pitchRotation = new Quaternionf().rotateX((float) Math.toRadians(-pitch));
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(pitchRotation);
        Vector3f translation = new Vector3f(offsetX, offsetY, 0.0F);
        int pixelScale = Math.max(1, (int) scale);
        InventoryScreen.renderEntityInInventory(graphics, (x0 + x1) / 2.0F, (y0 + y1) / 2.0F, pixelScale, translation, rotation, pitchRotation, entity);
    }
    *//*?} else if >=1.19.4 && <1.20.2 {*/
    /*private void renderEntityInBox(LivingEntity entity, int x0, int y0, int x1, int y1, float scale, float pitch, float offsetX, float offsetY) {
        Quaternionf pitchRotation = new Quaternionf().rotateX((float) Math.toRadians(-pitch));
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(pitchRotation);
        int pixelScale = Math.max(1, (int) scale);
        int feetX = Math.round((x0 + x1) / 2.0F + offsetX * pixelScale);
        int feetY = Math.round((y0 + y1) / 2.0F + offsetY * pixelScale);
        InventoryScreen.renderEntityInInventory(getGraphics(), feetX, feetY, pixelScale, rotation, pitchRotation, entity);
    }
    *//*?} else if >=1.19.3 && <1.19.4 {*/
    /*private void renderEntityInBox(LivingEntity entity, int x0, int y0, int x1, int y1, float scale, float pitch, float offsetX, float offsetY) {
        float feetX = (x0 + x1) / 2.0F + offsetX * scale;
        float feetY = (y0 + y1) / 2.0F + offsetY * scale;
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(feetX, feetY, 1050.0F);
        modelView.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        PoseStack entityPose = new PoseStack();
        entityPose.translate(0.0F, 0.0F, 1000.0F);
        entityPose.scale(scale, scale, scale);
        Quaternionf pitchRotation = new Quaternionf().rotateX((float) Math.toRadians(-pitch));
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(pitchRotation);
        entityPose.mulPose(rotation);

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        pitchRotation.conjugate();
        dispatcher.overrideCameraOrientation(pitchRotation);
        dispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, entityPose, buffers, 15728880));
        buffers.endBatch();
        dispatcher.setRenderShadow(true);

        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }
    *//*?} else if <1.19.3 {*/
    /*private void renderEntityInBox(LivingEntity entity, int x0, int y0, int x1, int y1, float scale, float pitch, float offsetX, float offsetY) {
        double feetX = (x0 + x1) / 2.0 + offsetX * scale;
        double feetY = (y0 + y1) / 2.0 + offsetY * scale;
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(feetX, feetY, 1050.0);
        modelView.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        PoseStack entityPose = new PoseStack();
        entityPose.translate(0.0, 0.0, 1000.0);
        entityPose.scale(scale, scale, scale);
        Quaternion pitchRotation = Vector3f.XP.rotationDegrees(-pitch);
        Quaternion rotation = Vector3f.ZP.rotationDegrees(180.0F);
        rotation.mul(pitchRotation);
        entityPose.mulPose(rotation);

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        pitchRotation.conj();
        dispatcher.overrideCameraOrientation(pitchRotation);
        dispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, entityPose, buffers, 15728880));
        buffers.endBatch();
        dispatcher.setRenderShadow(true);

        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }
    *//*?}*/

    static float fitScale(int boxWidth, int boxHeight, float entityWidth, float entityHeight) {
        float scale = Math.min(boxWidth / Math.max(entityWidth, 0.01F), boxHeight / Math.max(entityHeight, 0.01F));
        return scale * 0.9F;
    }

    // --- Scissor

    /**
     * Overloaded method of {@link #enableScissor(int, int, int, int)}
     * to use with and height to create the scissor rectangle
     *
     * @param x      left edge of the scissor rectangle
     * @param y      top edge of the scissor rectangle
     * @param width  width of the scissor rectangle in pixels
     * @param height height of the scissor rectangle in pixels
     * @see #enableScissor(int, int, int, int)
     */
    public void enableScissorWH(int x, int y, int width, int height) {
        enableScissor(x, y, x + width, y + height);
    }

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
     * @param x1 x position of top left corner of the scissor rectangle
     * @param y1 y position of top left corner of the scissor rectangle
     * @param x2 x position of bottom right corner of the scissor rectangle
     * @param y2 y position of bottom right corner of the scissor rectangle
     * @see #disableScissor()
     */
    public void enableScissor(int x1, int y1, int x2, int y2) {
        /*? if >=1.21.6 {*/
        graphics.enableScissor(x1, y1, x2, y2); // already follows the pose
        /*?} else {*/
        /*int[] r = mapThroughPose(x1, y1, x2, y2);
        enableScissorRaw(r[0], r[1], r[2], r[3]);
        *//*?}*/
    }

    private void enableScissorRaw(int x1, int y1, int x2, int y2) {
        /*? if >=1.20 {*/
        graphics.enableScissor(x1, y1, x2, y2);
        /*?} else if >=1.19.4 {*/
        //GuiComponent.enableScissor(x1, y1, x2, y2);
        /*?} else {*/
        //ScissorStack.push(x1, y1, x2, y2);
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
        /*?} else if >=1.19.4 {*/
        //GuiComponent.disableScissor();
        /*?} else {*/
        //ScissorStack.pop();
        /*?}*/
    }

    /*? if < 1.21.6 { */
    /*private int[] mapThroughPose(int x1, int y1, int x2, int y2) {
        /^? if >=1.20 {^/
        org.joml.Matrix4f m = graphics.pose().last().pose();
        /^?} else if >=1.19.3 {^/
        //org.joml.Matrix4f m = poseStack.last().pose();
        /^?} else {^/
        //com.mojang.math.Matrix4f m = poseStack.last().pose();
        /^?}^/

        /^? if >=1.19.3 {^/
        org.joml.Vector4f a = new org.joml.Vector4f(x1, y1, 0, 1).mul(m);
        org.joml.Vector4f b = new org.joml.Vector4f(x2, y2, 0, 1).mul(m);
        float ax = a.x, ay = a.y, bx = b.x, by = b.y;
        /^?} else {^/
        /^com.mojang.math.Vector4f a = new com.mojang.math.Vector4f(x1, y1, 0, 1);
        com.mojang.math.Vector4f b = new com.mojang.math.Vector4f(x2, y2, 0, 1);
        a.transform(m);
        b.transform(m);
        float ax = a.x(), ay = a.y(), bx = b.x(), by = b.y();
        ^//^?}^/

        return new int[]{Math.round(Math.min(ax, bx)), Math.round(Math.min(ay, by)),
                Math.round(Math.max(ax, bx)), Math.round(Math.max(ay, by))};
    }
    *//*? } */


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
                        drawWidth, drawHeight   // how big are on texture is
                );
                currentY += drawHeight;
            }
            currentX += drawWidth;
        }
    }

    /**
     * Draws a precise sub-rectangle of a texture - samples the region at
     * ({@code u}, {@code v}) sized {@code regionWidth} x {@code regionHeight} in
     * texture-space, and draws it on screen at ({@code x}, {@code y}) sized
     * {@code width} x {@code height} (stretched if the two sizes differ).
     *
     * <p>Use this to blit an arbitrary piece of a larger texture file (one border
     * segment out of a container texture, say) rather than a {@link GenericTexture}'s
     * own predefined region as a whole - {@link #drawTexture} always draws a
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
        //graphics.item(itemStack, x, y);
        /*?} else if >=1.20 { */
        graphics.renderItem(itemStack, x, y);
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
        //graphics.itemDecorations(font, itemStack, x, y, countText);
        /*?} else if >=1.20 { */
        graphics.renderItemDecorations(font, itemStack, x, y, countText);
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
     * rendering is always 16x16). Handles the push/translate/scale/pop pose
     * bookkeeping internally.
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
