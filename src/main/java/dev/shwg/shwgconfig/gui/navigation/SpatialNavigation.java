package dev.shwg.shwgconfig.gui.navigation;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.KeyboardFocusEntryPoint;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Arrow key navigation for versions without vanilla's (before 1.19.4), modelled on vanilla's
 * {@code ContainerEventHandler#nextFocusPath}:
 *
 * <ul>
 *     <li>It works <b>per container</b>: the focused container first tries to move inside itself;
 *     only if it can't, its parent picks among its own children - where a whole container
 *     (a scroll area, the tab bar) is one candidate, with its full bounds.</li>
 *     <li>Entering a container starts at its edge facing the focus, so arrowing down into the
 *     content lands on its top widget, not on whatever is closest to the old focus.</li>
 *     <li>A container implementing {@link KeyboardFocusEntryPoint} picks its entry child itself
 *     (e.g. the tab bar lands on the current tab).</li>
 * </ul>
 */
public final class SpatialNavigation {

    private SpatialNavigation() {}

    @Nullable
    public static SpatialDirection direction(GenericKeyEvent event) {
        if (event.isUp()) return SpatialDirection.UP;
        if (event.isDown()) return SpatialDirection.DOWN;
        if (event.isLeft()) return SpatialDirection.LEFT;
        if (event.isRight()) return SpatialDirection.RIGHT;
        return null;
    }

    /**
     * Moves the focus one step in {@code direction}.
     *
     * @param root      the screen (or another top level container)
     * @param ownBounds the root's bounds, used when nothing is focused yet
     * @return {@code true} if the focus moved
     */
    public static boolean navigate(ContainerEventHandler root, Rect ownBounds, SpatialDirection direction) {
        List<GuiEventListener> path = nextPath(root, ownBounds, direction);
        if (path == null) {
            return false;
        }
        applyPath(root, path);
        return true;
    }

    // --- Finding the next focus

    /**
     * The path (containers below {@code container}, then the leaf) to focus next, or {@code null}
     * if nothing inside {@code container} lies in that direction.
     */
    @Nullable
    private static List<GuiEventListener> nextPath(ContainerEventHandler container, Rect bounds, SpatialDirection direction) {
        GuiEventListener focused = container.getFocused();

        // 1. The focused child container tries to move inside itself first.
        if (focused instanceof ContainerEventHandler focusedContainer) {
            Rect focusedBounds = rectOf(focusedContainer);
            if (focusedBounds != null) {
                List<GuiEventListener> inner = nextPath(focusedContainer, focusedBounds, direction);
                if (inner != null) {
                    return prepend(focusedContainer, inner);
                }
            }
        }

        // 2. Otherwise pick among this container's own children, starting from the focused child,
        //    or from this container's edge facing the direction we came from.
        Rect origin = focused != null ? rectOf(focused) : null;
        if (origin == null) {
            origin = bounds.border(direction.opposite());
        }
        return pickChild(container, origin, direction, focused);
    }

    @Nullable
    private static List<GuiEventListener> pickChild(ContainerEventHandler container, Rect origin,
                                                    SpatialDirection direction, @Nullable GuiEventListener excluded) {
        SpatialAxis crossAxis = direction.axis().orthogonal();
        int originEdge = origin.boundInDirection(direction.opposite());

        // In line: ahead and overlapping on the cross axis. Nearest first, then the left- / top-most.
        List<GuiEventListener> inLine = new ArrayList<>();
        for (GuiEventListener child : container.children()) {
            if (child == excluded) continue;
            Rect rect = rectOf(child);
            if (rect == null || !rect.overlapsInAxis(origin, crossAxis)) continue;
            int childEdge = rect.boundInDirection(direction.opposite());
            boolean ahead = childEdge != originEdge
                    ? direction.isAfter(childEdge, originEdge)
                    : direction.isAfter(rect.boundInDirection(direction), origin.boundInDirection(direction))
                    && rect.boundInDirection(direction) != origin.boundInDirection(direction);
            if (ahead) inLine.add(child);
        }
        SpatialDirection crossPositive = crossAxis == SpatialAxis.HORIZONTAL ? SpatialDirection.RIGHT : SpatialDirection.DOWN;
        inLine.sort(Comparator
                .<GuiEventListener>comparingInt(child -> distanceAhead(origin, rectOf(child), direction))
                .thenComparingInt(child -> rectOf(child).boundInDirection(crossPositive.opposite())));
        for (GuiEventListener child : inLine) {
            List<GuiEventListener> path = enter(child, direction);
            if (path != null) return path;
        }

        // Roughly ahead: nearest by the distance between the facing edges' centers.
        List<GuiEventListener> ahead = new ArrayList<>();
        for (GuiEventListener child : container.children()) {
            if (child == excluded) continue;
            Rect rect = rectOf(child);
            if (rect != null && direction.isAfter(rect.boundInDirection(direction.opposite()), origin.boundInDirection(direction))) {
                ahead.add(child);
            }
        }
        ahead.sort(Comparator.comparingLong(child -> edgeCenterDistanceSquared(origin, rectOf(child), direction)));
        for (GuiEventListener child : ahead) {
            List<GuiEventListener> path = enter(child, direction);
            if (path != null) return path;
        }
        return null;
    }

    /**
     * The path to focus when navigation moves onto {@code child}: the child itself if it's a
     * focusable widget, or the entry path into it if it's a container. {@code null} if neither.
     */
    @Nullable
    private static List<GuiEventListener> enter(GuiEventListener child, SpatialDirection direction) {
        if (child instanceof ContainerEventHandler container) {
            if (child instanceof AbstractWidget widget && !widget.visible) {
                return null;
            }
            if (container instanceof KeyboardFocusEntryPoint entryPoint) {
                GuiEventListener entry = entryPoint.keyboardEntryFocus();
                if (entry != null) {
                    List<GuiEventListener> inner = enter(entry, direction); // the entry may be a container itself
                    if (inner != null) return prepend(container, inner);
                }
            }
            Rect bounds = rectOf(container);
            if (bounds == null) return null;
            // Entering from outside: start at the container's edge facing where we came from,
            // ignoring whatever child it may still remember as focused.
            List<GuiEventListener> inner = pickChild(container, bounds.border(direction.opposite()), direction, null);
            return inner == null ? null : prepend(container, inner);
        }
        return isFocusable(child) ? List.of(child) : null;
    }

    private static boolean isFocusable(GuiEventListener listener) {
        return listener instanceof AbstractWidget widget && widget.active && widget.visible;
    }

    private static int distanceAhead(Rect origin, Rect candidate, SpatialDirection direction) {
        int near = candidate.boundInDirection(direction.opposite());
        int edge = origin.boundInDirection(direction.opposite());
        return direction.isPositive() ? near - edge : edge - near;
    }

    private static long edgeCenterDistanceSquared(Rect origin, Rect candidate, SpatialDirection direction) {
        SpatialAxis crossAxis = direction.axis().orthogonal();
        long dPrimary = candidate.boundInDirection(direction.opposite()) - origin.boundInDirection(direction);
        long dCross = candidate.centerInAxis(crossAxis) - origin.centerInAxis(crossAxis);
        return dPrimary * dPrimary + dCross * dCross;
    }

    // --- Applying the new focus

    /**
     * Focuses {@code path} below {@code root}. Only the part that changes is touched: containers
     * that keep the focus get no focus lost / gained, like the 1.19.4+ fix in GenericScreen.
     */
    public static void applyPath(ContainerEventHandler root, List<GuiEventListener> path) {
        List<GuiEventListener> old = currentPath(root);
        int shared = 0;
        while (shared < old.size() && shared < path.size() && old.get(shared) == path.get(shared)) {
            shared++;
        }

        // Unfocus the old branch, deepest first.
        for (int i = old.size() - 1; i >= shared; i--) {
            GuiEventListener listener = old.get(i);
            if (listener instanceof ContainerEventHandler container) container.setFocused(null);
            if (listener instanceof GenericAbstractWidget widget) widget.setFocused(false);
        }

        // Focus the new branch, top first.
        ContainerEventHandler parent = shared == 0 ? root : (ContainerEventHandler) path.get(shared - 1);
        for (int i = shared; i < path.size(); i++) {
            GuiEventListener listener = path.get(i);
            parent.setFocused(listener);
            if (listener instanceof GenericAbstractWidget widget) widget.setFocused(true);
            if (listener instanceof ContainerEventHandler container) parent = container;
        }
    }

    public static @Nullable List<GuiEventListener> entryPath(GuiEventListener child, SpatialDirection direction) {
        return enter(child, direction);
    }

    private static List<GuiEventListener> currentPath(ContainerEventHandler root) {
        List<GuiEventListener> path = new ArrayList<>();
        GuiEventListener focused = root.getFocused();
        while (focused != null) {
            path.add(focused);
            focused = focused instanceof ContainerEventHandler container ? container.getFocused() : null;
        }
        return path;
    }

    private static List<GuiEventListener> prepend(GuiEventListener first, List<GuiEventListener> rest) {
        List<GuiEventListener> path = new ArrayList<>(rest.size() + 1);
        path.add(first);
        path.addAll(rest);
        return path;
    }

    @Nullable
    private static Rect rectOf(GuiEventListener listener) {
        if (listener instanceof GenericAbstractWidget widget) {
            return Rect.of(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
        }
        return null; // vanilla's own widgets differ too much across 1.19 - 1.19.3, ignoring it
    }
}
