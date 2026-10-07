package dev.shwg.shwgconfig.gui.input.events;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.api.values.stringvalues.KeybindValue;
/*? if >= 1.21.9 { */
import net.minecraft.client.input.KeyEvent;
/*? } else {*/
//import org.lwjgl.glfw.GLFW;
/*? } */

public class GenericKeyEvent {

    /*? if >= 1.21.9 */
    private final KeyEvent keyEvent;

    private final int key;
    private final int scanCode;
    private final int modifiers;
    private final boolean shiftDown;
    private final boolean altDown;
    private final boolean ctrlDown;
    private final boolean isSelection;

    /*? if >= 1.21.9 { */
    public GenericKeyEvent(KeyEvent keyEvent) {
        this.modifiers = keyEvent.modifiers();
        this.key = keyEvent.key();
        /*? if >= 26.3 {*/
        //this.scanCode = keyEvent.keycode();
        /*? } else {*/
        this.scanCode = keyEvent.scancode();
        /*? } */
        this.shiftDown = keyEvent.hasShiftDown();
        this.altDown = keyEvent.hasAltDown();
        this.ctrlDown = keyEvent.hasControlDown();
        this.isSelection = keyEvent.isSelection();
        this.keyEvent = keyEvent;
    }
    /*? } else {*/
    /*public GenericKeyEvent(int keyCode, int scanCode, int modifiers) {
        this.key = keyCode;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
        this.shiftDown = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        this.altDown = (modifiers & GLFW.GLFW_MOD_ALT) != 0;
        this.ctrlDown = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        this.isSelection = (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_KP_ENTER);
    }
    *//*? } */

    public int key() {
        return key;
    }

    public int getScanCode() {
        return scanCode;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean hasShiftDown() {
        return shiftDown;
    }

    public boolean hasAltDown() {
        return altDown;
    }

    public boolean hasControlDown() {
        return ctrlDown;
    }

    public boolean isSelection() {
        return isSelection;
    }

    public boolean isConfirmation() {
        return key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER;
    }

    public boolean isBackspace() {
        return key == InputConstants.KEY_BACKSPACE;
    }

    public boolean isEscape() {
        return key == InputConstants.KEY_ESCAPE;
    }

    public boolean isLeft() {
        return key == InputConstants.KEY_LEFT;
    }

    public boolean isRight() {
        return key == InputConstants.KEY_RIGHT;
    }

    public boolean isUp() {
        return key == InputConstants.KEY_UP;
    }

    public boolean isDown() {
        return key == InputConstants.KEY_DOWN;
    }

    public boolean isTab() {
        return key == InputConstants.KEY_TAB;
    }

    public boolean isSelectAll() {
        return key == InputConstants.KEY_A && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public boolean isCopy() {
        return key == InputConstants.KEY_C && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public boolean isPaste() {
        return key == InputConstants.KEY_V && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public boolean isCut() {
        return key == InputConstants.KEY_X && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public InputConstants.Key getKey() {
        return InputConstants.Type.KEYSYM.getOrCreate(key);
    }

    /**
     *
     * @param key use {@link InputConstants} constants as key
     * @return whether key matches this keyevent's key
     */
    public boolean matches(final int key) {
        return getKey().getValue() == key;
    }

    /*? if >= 1.21.9 { */
    public KeyEvent getKeyEvent() {
        return keyEvent;
    }
    /*? } */

    /**
     * Checks whether the pressed button matches those button specified in the {@link KeybindValue}.
     * @param keybindValue KeybindValue to check for buttons
     * @return whether the pressed button matches.
     */
    public boolean matches(KeybindValue keybindValue) {
        /*? if >= 1.21.9 { */
        return keybindValue.keyMapping().matches(keyEvent);
        /*? } else {*/
        //return keybindValue.keyMapping().matches(key, scanCode);
        /*? } */
    }
}
