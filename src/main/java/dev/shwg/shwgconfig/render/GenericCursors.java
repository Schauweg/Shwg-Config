package dev.shwg.shwgconfig.render;
/*? if >= 1.21.9 { */
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
/*? } */

public enum GenericCursors {
    ARROW,
    TEXT,
    CROSSHAIR,
    POINTING_HAND,
    RESIZE_NS,
    RESIZE_EW,
    RESIZE_ALL,
    NOT_ALLOWED;

    /*? if >= 1.21.9 { */
    public CursorType getVanillaType() {
        return switch (this) {
            case TEXT -> CursorTypes.IBEAM;
            case CROSSHAIR -> CursorTypes.CROSSHAIR;
            case POINTING_HAND -> CursorTypes.POINTING_HAND;
            case RESIZE_NS -> CursorTypes.RESIZE_NS;
            case RESIZE_EW -> CursorTypes.RESIZE_EW;
            case RESIZE_ALL -> CursorTypes.RESIZE_ALL;
            case NOT_ALLOWED -> CursorTypes.NOT_ALLOWED;
            default -> CursorTypes.ARROW;
        };
    }
    /*? } else { */
    //public void getVanillaType() { }
    /*? } */
}
