package dev.shwg.testmod.test.interaction;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;

/*? if >=1.21.9 {*/
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
/*?}*/

/**
 * Simulated input for the interaction test: calls the screen's own input methods, exactly like Minecraft does
 * when GLFW reports real input. So the whole chain is tested (GenericScreen, OverlayDispatch, focus, the widget),
 * not just one widget method.
 *
 * <p>The version conditions for the input method signatures live only here: 1.21.9 switched to
 * MouseButtonEvent / KeyEvent / CharacterEvent, 26 dropped the modifiers from CharacterEvent.</p>
 */
public final class InputSim {

    public static final int LEFT = InputConstants.MOUSE_BUTTON_LEFT;
    public static final int RIGHT = InputConstants.MOUSE_BUTTON_RIGHT;

    private InputSim() {
    }

    /** Press and release of a mouse button at (x, y). */
    public static void click(Screen screen, double x, double y, int button) {
        press(screen, x, y, button);
        release(screen, x, y, button);
    }

    public static boolean press(Screen screen, double x, double y, int button) {
        /*? if >=1.21.9 {*/
        return screen.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0)), false);
        /*?} else {*/
        //return screen.mouseClicked(x, y, button);
        /*?}*/
    }

    public static boolean release(Screen screen, double x, double y, int button) {
        /*? if >=1.21.9 {*/
        return screen.mouseReleased(new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0)));
        /*?} else {*/
        //return screen.mouseReleased(x, y, button);
        /*?}*/
    }

    /** Press and release of a key (an InputConstants.KEY_... code), without modifiers. */
    public static void key(Screen screen, int keyCode) {
        keyPress(screen, keyCode);
        keyRelease(screen, keyCode);
    }

    public static boolean keyPress(Screen screen, int keyCode) {
        /*? if >=1.21.9 {*/
        return screen.keyPressed(new KeyEvent(keyCode, 0, 0));
        /*?} else {*/
        //return screen.keyPressed(keyCode, 0, 0);
        /*?}*/
    }

    public static boolean keyRelease(Screen screen, int keyCode) {
        /*? if >=1.21.9 {*/
        return screen.keyReleased(new KeyEvent(keyCode, 0, 0));
        /*?} else {*/
        //return screen.keyReleased(keyCode, 0, 0);
        /*?}*/
    }

    /** Types the text character by character, like the keyboard's character events do. */
    public static void type(Screen screen, String text) {
        text.codePoints().forEach(codepoint -> typeChar(screen, codepoint));
    }

    public static boolean typeChar(Screen screen, int codepoint) {
        /*? if >=26 {*/
        //return screen.charTyped(new CharacterEvent(codepoint));
        /*?} else if >=1.21.9 {*/
        return screen.charTyped(new CharacterEvent(codepoint, 0));
        /*?} else {*/
        //return screen.charTyped((char) codepoint, 0);
        /*?}*/
    }
}
