package dev.shwg.shwgconfig.gui.input.events;

import com.mojang.blaze3d.platform.InputConstants;

/*? if >= 1.21.9 { */
import net.minecraft.client.input.MouseButtonEvent;
/*?} else {*/
//import net.minecraft.client.gui.screens.Screen;
/*?}*/

public class GenericMouseButtonEvent {

    /*? if < 1.21.9 { */
    /*private static final long DOUBLE_CLICK_THRESHOLD_MS = 250L;
    private static final double DOUBLE_CLICK_POSITION_THRESHOLD_SQ = 3.0 * 3.0;

    private static long lastClickTimeMs = -1;
    private static int lastClickButton = -1;
    private static double lastClickX = 0;
    private static double lastClickY = 0;

    // Decided exactly once per physical click, via beginClick() - not re-derived by every
    // GenericMouseButtonEvent constructed for that same click. Multiple constructions per
    // click are normal here (container pre-checks, the old "ask every sibling" dispatch,
    // the actual target widget) - re-deriving independently in each one is what caused this,
    // since each construction's own clock-update stomped on the next.
    private static boolean currentClickIsDouble = false;
    *//*? } */

    private final double mouseX;
    private final double mouseY;
    private final int button;
    private final int modifiers;
    private final boolean shiftDown;
    private final boolean altDown;
    private final boolean ctrlDown;
    private final boolean doubleClick;

    /*? if >= 1.21.9 { */
    public GenericMouseButtonEvent(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        this.mouseX = mouseButtonEvent.x();
        this.mouseY = mouseButtonEvent.y();
        this.button = mouseButtonEvent.button();
        this.modifiers = mouseButtonEvent.modifiers();
        this.shiftDown = mouseButtonEvent.hasShiftDown();
        this.altDown = mouseButtonEvent.hasAltDown();
        this.ctrlDown = mouseButtonEvent.hasControlDown();
        this.doubleClick = doubleClick;
    }
    /*? } else {*/

    /*// Call exactly once, from GenericScreen.mouseClicked, before any dispatch happens -
    // the single point every click genuinely enters this system through.
    public static void beginClick(double mouseX, double mouseY, int button) {
        long now = System.currentTimeMillis();
        currentClickIsDouble = button != -1
                && button == lastClickButton
                && (now - lastClickTimeMs) <= DOUBLE_CLICK_THRESHOLD_MS
                && isWithinPositionThreshold(mouseX, mouseY, lastClickX, lastClickY);

        lastClickTimeMs = now;
        lastClickButton = button;
        lastClickX = mouseX;
        lastClickY = mouseY;
    }

    public GenericMouseButtonEvent(double mouseX, double mouseY, int button, boolean mouseDown) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
        this.modifiers = 0;
        this.shiftDown = Screen.hasShiftDown();
        this.altDown = Screen.hasAltDown();
        this.ctrlDown = Screen.hasControlDown();
        this.doubleClick = mouseDown && currentClickIsDouble;
    }

    public GenericMouseButtonEvent(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = -1;
        this.modifiers = 0;
        this.shiftDown = Screen.hasShiftDown();
        this.altDown = Screen.hasAltDown();
        this.ctrlDown = Screen.hasControlDown();
        this.doubleClick = false;
    }

    private static boolean isWithinPositionThreshold(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return (dx * dx + dy * dy) <= DOUBLE_CLICK_POSITION_THRESHOLD_SQ;
    }
    *//*? } */

    public boolean mouseInfoAvailable() {
        return button > -1;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public int getButton() {
        return button;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isLeftClick() {
        return button == InputConstants.MOUSE_BUTTON_LEFT;
    }

    public boolean isRightClick() {
        return button == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    public boolean isMiddleClick() {
        return button == InputConstants.MOUSE_BUTTON_MIDDLE;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public boolean isAltDown() {
        return altDown;
    }

    public boolean isCtrlDown() {
        return ctrlDown;
    }

    public boolean isDoubleClick() {
        return doubleClick;
    }

    public InputConstants.Key getKey() {
        return InputConstants.Type.MOUSE.getOrCreate(button);
    }
}
