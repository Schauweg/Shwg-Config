/*? if < 1.19.4 { */

/*package dev.shwg.shwgconfig.render.scissor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorStack {

    private static final Deque<ScissorRect> stack = new ArrayDeque<>();

    // Corner coordinates in GUI space; x2/y2 are exclusive. May be empty (x2 <= x1 or y2 <= y1).
    private record ScissorRect(int x1, int y1, int x2, int y2) {
        // Intersection with another rect. Empty (but never null) if they don't overlap.
        ScissorRect intersect(ScissorRect other) {
            int nx1 = Math.max(x1, other.x1);
            int ny1 = Math.max(y1, other.y1);
            int nx2 = Math.min(x2, other.x2);
            int ny2 = Math.min(y2, other.y2);
            return (nx2 > nx1 && ny2 > ny1) ? new ScissorRect(nx1, ny1, nx2, ny2) : EMPTY;
        }

        boolean isEmpty() {
            return x2 <= x1 || y2 <= y1;
        }
    }

    private static final ScissorRect EMPTY = new ScissorRect(0, 0, 0, 0);

    // Push a new scissor area (corner coordinates in GUI space), clipped to the current one.
    public static void push(int x1, int y1, int x2, int y2) {
        ScissorRect rect = new ScissorRect(x1, y1, x2, y2);
        if (rect.isEmpty()) rect = EMPTY;
        if (!stack.isEmpty()) rect = stack.peek().intersect(rect);
        stack.push(rect);
        applyCurrentScissor();
    }

    // Pop the last scissor area.
    public static void pop() {
        if (!stack.isEmpty()) stack.pop();
        applyCurrentScissor();
    }

    private static void applyCurrentScissor() {
        if (stack.isEmpty()) {
            RenderSystem.disableScissor();
            return;
        }
        ScissorRect rect = stack.peek();
        if (rect.isEmpty()) {
            RenderSystem.enableScissor(0, 0, 0, 0); // clip everything
            return;
        }
        enableScissorRaw(rect.x1, rect.y1, rect.x2, rect.y2);
    }

    // Converts GUI space corners to window pixels (origin bottom left) and enables the GL scissor.
    private static void enableScissorRaw(int x1, int y1, int x2, int y2) {
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();

        int scissorX = (int) (x1 * scale);
        int scissorY = (int) (window.getHeight() - y2 * scale);
        int scissorW = (int) ((x2 - x1) * scale);
        int scissorH = (int) ((y2 - y1) * scale);

        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
    }

    public static void disable() {
        stack.clear();
        RenderSystem.disableScissor();
    }
}
*//*? } */
