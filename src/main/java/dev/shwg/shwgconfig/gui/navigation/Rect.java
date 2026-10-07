package dev.shwg.shwgconfig.gui.navigation;

public record Rect(int left, int top, int right, int bottom) {

    public static Rect of(int x, int y, int width, int height) {
        return new Rect(x, y, x + width, y + height);
    }

    public int boundInDirection(SpatialDirection direction) {
        return switch (direction) {
            case UP -> top;
            case DOWN -> bottom;
            case LEFT -> left;
            case RIGHT -> right;
        };
    }

    public int centerInAxis(SpatialAxis axis) {
        return axis == SpatialAxis.HORIZONTAL ? (left + right) / 2 : (top + bottom) / 2;
    }

    public boolean overlapsInAxis(Rect other, SpatialAxis axis) {
        if (axis == SpatialAxis.HORIZONTAL) {
            return Math.max(left, other.left) <= Math.min(right, other.right);
        }
        return Math.max(top, other.top) <= Math.min(bottom, other.bottom);
    }

    /** A zero-thickness line just past this rect's edge facing {@code direction}, spanning its full perpendicular extent. */
    public Rect border(SpatialDirection direction) {
        int edge = boundInDirection(direction);
        return switch (direction.axis()) {
            case VERTICAL -> new Rect(left, edge, right, edge);
            case HORIZONTAL -> new Rect(edge, top, edge, bottom);
        };
    }
}
