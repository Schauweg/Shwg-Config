/*? if < 1.19.4 { */

/*package dev.shwg.shwgconfig.render.scissor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorStack {

    private static final Deque<ScissorRect> stack = new ArrayDeque<>();

    private record ScissorRect(int x, int y, int width, int height) {
        // Returns intersection with another rect, or null if no overlap
        public ScissorRect intersect(ScissorRect other) {
            int x1 = Math.max(this.x, other.x);
            int y1 = Math.max(this.y, other.y);
            int x2 = Math.min(this.x + this.width, other.x + other.width);
            int y2 = Math.min(this.y + this.height, other.y + other.height);

            int w = x2 - x1;
            int h = y2 - y1;

            return (w > 0 && h > 0) ? new ScissorRect(x1, y1, w, h) : null;
        }
    }

    /^*
     * Push a new scissor area (relative to screen)
     ^/
    public static void push(int x, int y, int width, int height) {
        ScissorRect newRect = new ScissorRect(x, y, width, height);

        ScissorRect finalRect = newRect;
        if (!stack.isEmpty()) {
            finalRect = stack.peek().intersect(newRect);
        }

        stack.push(finalRect != null ? finalRect : new ScissorRect(x, y, width, height));

        applyCurrentScissor();
    }

    /^*
     * Pop the last scissor area
     ^/
    public static void pop() {
        if (!stack.isEmpty()) {
            stack.pop();
        }
        applyCurrentScissor();
    }

    private static void applyCurrentScissor() {
        if (stack.isEmpty()) {
            RenderSystem.disableScissor();
            return;
        }

        ScissorRect rect = stack.peek();
        if (rect.width <= 0 || rect.height <= 0) {
            RenderSystem.disableScissor();
            return;
        }

        enableScissorRaw(rect.x, rect.y, rect.width, rect.height);
    }

    /^*
     * Enable scissor using raw coordinates (for 1.19)
     ^/
    private static void enableScissorRaw(int x, int y, int width, int height) {
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();

        int scissorX = (int) (x * scale);
        int scissorY = (int) (window.getHeight() - (y + height) * scale);
        int scissorW = (int) (width * scale);
        int scissorH = (int) (height * scale);

        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
    }

    public static void disable() {
        stack.clear();
        RenderSystem.disableScissor();
    }
}
*//*? } */
