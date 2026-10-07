package dev.shwg.shwgconfig.gui.navigation;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    public static boolean navigate(ContainerEventHandler rootContainer, Rect ownBounds, SpatialDirection direction) {
        List<Candidate> flattenCandidates = new ArrayList<>();
        flattenCandidates(rootContainer, flattenCandidates, new ArrayList<>());
        return navigate(rootContainer, ownBounds, direction, flattenCandidates);
    }

    private static boolean navigate(ContainerEventHandler container, Rect ownBounds, SpatialDirection direction, List<Candidate> candidates) {
        GuiEventListener focused = container.getFocused();

        if (focused instanceof ContainerEventHandler focusedContainer) {
            Rect focusedBounds = rectOf(focusedContainer);
            if (focusedBounds != null && navigate(focusedContainer, focusedBounds, direction, candidates)) {
                return true;
            }
        }

        Rect searchOrigin;
        if (focused != null) {
            GuiEventListener deepest = deepestFocused(focused);
            searchOrigin = borderForArrowNavigation(new Candidate(deepest, List.of()), direction);
        } else {
            searchOrigin = ownBounds.border(direction.opposite());
        }

        List<Candidate> scoped = candidates.stream()
                .filter(c -> c.eventHandler().contains(container))
                .toList();

        List<Candidate> lane = laneMatches(scoped, searchOrigin, direction);
        for (Candidate candidate : lane) {
            if (tryFocus(container, candidate)) {
                return true;
            }
        }

        List<Candidate> loose = looseMatches(scoped, searchOrigin, direction);
        for (Candidate candidate : loose) {
            if (tryFocus(container, candidate)) {
                return true;
            }
        }

        return false;
    }

    private static void flattenCandidates(ContainerEventHandler container, List<Candidate> flattenCandidates, List<ContainerEventHandler> pathSoFar) {
        List<ContainerEventHandler> path = new ArrayList<>(pathSoFar);
        path.add(container);

        for (GuiEventListener child : container.children()) {
            if (!(child instanceof ContainerEventHandler)) {
                flattenCandidates.add(new Candidate(child, List.copyOf(path)));
            }
        }
        for (GuiEventListener child : container.children()) {
            if (child instanceof ContainerEventHandler eventHandler) {
                flattenCandidates(eventHandler, flattenCandidates, path);
            }
        }
    }

    private static GuiEventListener deepestFocused(GuiEventListener listener) {
        while (listener instanceof ContainerEventHandler containerEventHandler) {
            GuiEventListener childFocused = containerEventHandler.getFocused();
            if (childFocused == null) {
                break;
            }
            listener = childFocused;
        }
        return listener;
    }

    private static boolean tryFocus(ContainerEventHandler container, Candidate candidate) {
        container.setFocused(null);

        List<ContainerEventHandler> handler = candidate.eventHandler;
        for (int i = 0; i < handler.size() - 1; i++) {
            ContainerEventHandler eventHandler = handler.get(i);
            eventHandler.setFocused(handler.get(i + 1));
        }

        handler.get(handler.size() -1 ).setFocused(candidate.listener);
        if (candidate.listener instanceof GenericAbstractWidget widget) {
            widget.setFocused(true);
        }
        return true;
    }

    @Nullable
    private static Rect borderForArrowNavigation(Candidate candidate, SpatialDirection direction) {
        Rect rect = rectOf(candidate);
        return rect != null ? rect.border(direction) : null;
    }

    private static List<Candidate> laneMatches(List<Candidate> candidates, Rect origin, SpatialDirection direction) {
        List<Candidate> result = new ArrayList<>();
        for (Candidate candidate : candidates) {
            Rect rect = rectOf(candidate);
            if (rect != null && isAhead(origin, rect, direction) && origin.overlapsInAxis(rect, direction.axis().orthogonal())) {
                result.add(candidate);
            }
        }
        result.sort(Comparator.comparingInt(c -> primaryDistance(origin, rectOf(c), direction)));
        return result;
    }

    private static List<Candidate> looseMatches(List<Candidate> candidates, Rect origin, SpatialDirection direction) {
        List<Candidate> result = new ArrayList<>();
        for (Candidate candidate : candidates) {
            Rect rect = rectOf(candidate);
            if (rect != null && isAhead(origin, rect, direction)) {
                result.add(candidate);
            }
        }
        result.sort(Comparator.comparingDouble(c -> centerDistanceSquared(origin, rectOf(c), direction)));
        return result;
    }

    private static boolean isAhead(Rect origin, Rect candidate, SpatialDirection direction) {
        return direction.isAfter(candidate.boundInDirection(direction.opposite()), origin.boundInDirection(direction));
    }

    private static int primaryDistance(Rect origin, Rect candidate, SpatialDirection direction) {
        int near = candidate.boundInDirection(direction.opposite());
        int edge = origin.boundInDirection(direction);
        return direction.isPositive() ? (near - edge) : (edge - near);
    }

    private static double centerDistanceSquared(Rect origin, Rect candidate, SpatialDirection direction) {
        SpatialAxis orthogonal = direction.axis().orthogonal();
        double dPrimary = candidate.boundInDirection(direction.opposite()) - origin.boundInDirection(direction);
        double dSecondary = candidate.centerInAxis(orthogonal) - origin.centerInAxis(orthogonal);
        return dPrimary * dPrimary + dSecondary * dSecondary;
    }

    @Nullable
    private static Rect rectOf(Candidate candidate) {
        if (candidate.listener instanceof GenericAbstractWidget widget) {
            return Rect.of(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
        }
        return null;
    }
    @Nullable
    private static Rect rectOf(GuiEventListener candidate) {
        if (candidate instanceof GenericAbstractWidget widget) {
            return Rect.of(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
        }
        return null;
    }

    private record Candidate(GuiEventListener listener, List<ContainerEventHandler> eventHandler){}
}
