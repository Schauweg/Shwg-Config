package dev.shwg.testmod.test.interaction;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;

/*? if >=1.21.9 {*/
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
/*?}*/

/*? if >=26.3 {*/
//import org.lwjgl.sdl.SDLKeyboard;
/*?}*/
public final class InputSim {

    public static final int LEFT = InputConstants.MOUSE_BUTTON_LEFT;
    public static final int RIGHT = InputConstants.MOUSE_BUTTON_RIGHT;

    private InputSim() {
    }

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

    public static void key(Screen screen, int keyCode) {
        keyPress(screen, keyCode);
        keyRelease(screen, keyCode);
    }

    public static boolean keyPress(Screen screen, int keyCode) {
        /*? if >=1.21.9 {*/
        return screen.keyPressed(keyEvent(keyCode));
        /*?} else {*/
        //return screen.keyPressed(keyCode, 0, 0);
        /*?}*/
    }

    public static boolean keyRelease(Screen screen, int keyCode) {
        /*? if >=1.21.9 {*/
        return screen.keyReleased(keyEvent(keyCode));
        /*?} else {*/
        //return screen.keyReleased(keyCode, 0, 0);
        /*?}*/
    }

    /*? if >=26.3 {*/
    /*private static KeyEvent keyEvent(int key) {
        return new KeyEvent(key, SDLKeyboard.SDL_GetKeyFromScancode(key, (short) 0, false), 0);
    }
    *//*?} else if >=1.21.9 {*/
    private static KeyEvent keyEvent(int key) {
        return new KeyEvent(key, 0, 0);
    }
    /*?}*/

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
