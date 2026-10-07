package dev.shwg.shwgconfig.gui.navigation;

public enum SpatialDirection {
    UP(SpatialAxis.VERTICAL, false),
    DOWN(SpatialAxis.VERTICAL, true),
    LEFT(SpatialAxis.HORIZONTAL, false),
    RIGHT(SpatialAxis.HORIZONTAL, true);

    private final SpatialAxis axis;
    private final boolean positive;

    SpatialDirection(SpatialAxis axis, boolean positive) {
        this.axis = axis;
        this.positive = positive;
    }

    public SpatialAxis axis() {
        return axis;
    }

    public boolean isPositive() {
        return positive;
    }

    public SpatialDirection opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    /** True if {@code a} lies further along this direction than {@code b}. */
    public boolean isAfter(int a, int b) {
        return positive ? a >= b : b >= a;
    }
}
