package dev.shwg.shwgconfig.gui.navigation;

public enum SpatialAxis {
    HORIZONTAL,
    VERTICAL;

    public SpatialAxis orthogonal() {
        return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
    }
}
