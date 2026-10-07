package dev.shwg.shwgconfig.gui.input;

import com.mojang.blaze3d.platform.InputConstants;

public class LastInput {

    public static Input input = new Input(false, false, -1);

    public LastInput(boolean mouse, boolean keyboard, int key) {
        input = new Input(mouse, keyboard, key);
    }

    public static Input mouse(int button) {
        return new Input(true, false, button);
    }

    public static Input keyboard(int key) {
        return new Input(false, true, key);
    }

    public static boolean isMouse() {
        return input.isMouse();
    }

    public static boolean isKeyboard() {
        return input.isKeyboard();
    }

    public static boolean isTab() {
        return isKeyboard() && getKey() == InputConstants.KEY_TAB;
    }

    public static int getKey() {
        return input.button();
    }

    public record Input(boolean isMouse, boolean isKeyboard, int button) {

    }
}
