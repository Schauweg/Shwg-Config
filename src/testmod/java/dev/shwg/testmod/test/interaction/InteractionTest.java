package dev.shwg.testmod.test.interaction;

import dev.shwg.testmod.test.AutoTestSelection;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;

import static dev.shwg.testmod.test.AutoTestReport.require;

public final class InteractionTest {

    private static final int SETTLE = 3;

    private InteractionTest() {
    }

    public static void start(Screen parent, Runnable onDone) {
        InteractionTestScreen screen = new InteractionTestScreen(parent);
        InteractionScript script = new InteractionScript(onDone);
        script.waitTicks(20);
        buttons(script, screen);
        inputs(script, screen);
        screen.setTickHook(script::tick);
        screen.setScreen(screen);
    }

    // 3a: button, toggle, cycle button

    private static void buttons(InteractionScript s, InteractionTestScreen screen) {
        // Compared with the state right before the click, so an earlier failing step doesn't fail this one too.
        String[] before = new String[1];

        s.act("click the button", () -> screen.click(screen.button))
                .waitTicks(SETTLE)
                .check("button: a click presses it once",
                        () -> require(screen.buttonPresses == 1, "pressed " + screen.buttonPresses + " times"))

                .act("press Enter on the focused button", () -> screen.key(InputConstants.KEY_RETURN))
                .waitTicks(SETTLE)
                .check("button: Enter presses the focused button",
                        () -> require(screen.buttonPresses == 2, "pressed " + screen.buttonPresses + " times"))

                .act("click the toggle", () -> screen.click(screen.toggle))
                .waitTicks(SETTLE)
                .check("toggle: a click switches ON to OFF",
                        () -> require(!screen.toggleValue, "value is " + screen.toggleValue))

                .act("click the toggle again", () -> screen.click(screen.toggle))
                .waitTicks(SETTLE)
                .check("toggle: a second click switches back to ON",
                        () -> require(screen.toggleValue, "value is " + screen.toggleValue))

                .act("left click the cycle button", () -> screen.click(screen.cycle))
                .waitTicks(SETTLE)
                .check("cycle: left click goes forward (Banana -> Cherry)",
                        () -> require("Cherry".equals(screen.cycleValue), "value is " + screen.cycleValue))

                .act("left click the cycle button again", () -> screen.click(screen.cycle))
                .waitTicks(SETTLE)
                .check("cycle: forward wraps around (Cherry -> Apple)",
                        () -> require("Apple".equals(screen.cycleValue), "value is " + screen.cycleValue))

                .act("right click the cycle button", () -> screen.rightClick(screen.cycle))
                .waitTicks(SETTLE)
                .check("cycle: right click goes back and wraps (Apple -> Cherry)",
                        () -> require("Cherry".equals(screen.cycleValue), "value is " + screen.cycleValue))

                .act("click an empty spot", () -> {
                    before[0] = state(screen);
                    screen.clickAt(InteractionTestScreen.EMPTY_X, InteractionTestScreen.EMPTY_Y, InputSim.LEFT);
                })
                .waitTicks(SETTLE)
                .check("clicking next to the widgets changes nothing",
                        () -> require(state(screen).equals(before[0]), "before " + before[0] + ", after " + state(screen)))

                .waitTicks(SETTLE)
                .screenshot("buttons");
    }

    // 3b: text, number and color inputs

    private static void inputs(InteractionScript s, InteractionTestScreen screen) {
        s.act("replace the text", () -> {
                    screen.clear(screen.textBox);
                    screen.type("Shwg");
                })
                .waitTicks(SETTLE)
                .check("text: typed text is applied",
                        () -> require("Shwg".equals(screen.textValue), "value is '" + screen.textValue + "'"))

                .act("type 73 into the int field", () -> {
                    screen.clear(screen.intBox);
                    screen.type("73");
                })
                .waitTicks(SETTLE)
                .check("int: a valid number is applied while typing",
                        () -> require(screen.intValue == 73, "value is " + screen.intValue))

                .act("type 500 into the int field (max 100)", () -> {
                    screen.clear(screen.intBox);
                    screen.type("500");
                })
                .waitTicks(SETTLE)
                .check("int: a number above max is not applied",
                        () -> require(screen.intValue != 500 && screen.intValue <= 100, "value is " + screen.intValue))
                .check("int: the field still shows what was typed",
                        () -> require("500".equals(screen.intBox.getValue()), "text is '" + screen.intBox.getValue() + "'"))
                .screenshot("int-above-max")

                .act("press Tab to leave the int field", () -> screen.key(InputConstants.KEY_TAB))
                .waitTicks(SETTLE)
                .check("int: leaving the field clamps to max and applies it",
                        () -> require(screen.intValue == 100 && "100".equals(screen.intBox.getValue()),
                                "value " + screen.intValue + ", text '" + screen.intBox.getValue() + "'"))

                .act("type letters into the int field", () -> {
                    screen.click(screen.intBox);
                    screen.key(InputConstants.KEY_END);
                    screen.type("abc");
                })
                .waitTicks(SETTLE)
                .check("int: letters are filtered out",
                        () -> require("100".equals(screen.intBox.getValue()), "text is '" + screen.intBox.getValue() + "'"))

                .act("press Down in the int field", () -> screen.key(InputConstants.KEY_DOWN))
                .waitTicks(SETTLE)
                .check("int: Down lowers the value by one step",
                        () -> require(screen.intValue == 99, "value is " + screen.intValue))

                .act("type 0.5 into the float field", () -> {
                    screen.clear(screen.floatBox);
                    screen.type("0.5");
                })
                .waitTicks(SETTLE)
                .check("float: a decimal number is applied",
                        () -> require(Math.abs(screen.floatValue - 0.5f) < 1e-6, "value is " + screen.floatValue))

                .act("type 3.25 into the double field", () -> {
                    screen.clear(screen.doubleBox);
                    screen.type("3.25");
                })
                .waitTicks(SETTLE)
                .check("double: a decimal number is applied",
                        () -> require(Math.abs(screen.doubleValue - 3.25) < 1e-9, "value is " + screen.doubleValue))

                .act("type a full ARGB color", () -> {
                    screen.clear(screen.argbBox);
                    screen.type("80FF8800");
                })
                .waitTicks(SETTLE)
                .check("argb: a complete hex code is applied",
                        () -> require(screen.argbValue == 0x80FF8800, "value is " + hex(screen.argbValue)))

                .act("type an incomplete ARGB color", () -> {
                    screen.clear(screen.argbBox);
                    screen.type("FF0");
                })
                .waitTicks(SETTLE)
                .check("argb: an incomplete hex code is not applied",
                        () -> require(screen.argbValue == 0x80FF8800, "value is " + hex(screen.argbValue)))

                .act("type a lowercase RGB color", () -> {
                    screen.clear(screen.rgbBox);
                    screen.type("3399ff");
                })
                .waitTicks(SETTLE)
                .check("rgb: lowercase becomes uppercase",
                        () -> require("#3399FF".equals(screen.rgbBox.getValue()), "text is '" + screen.rgbBox.getValue() + "'"))
                .check("rgb: the color is applied with full alpha",
                        () -> require(screen.rgbValue == 0xFF3399FF, "value is " + hex(screen.rgbValue)))

                .act("press Tab to leave the RGB field", () -> screen.key(InputConstants.KEY_TAB))
                .waitTicks(SETTLE)
                .screenshot("inputs");
    }

    private static String hex(int color) {
        return String.format("#%08X", color);
    }

    private static String state(InteractionTestScreen screen) {
        return "presses " + screen.buttonPresses + ", toggle " + screen.toggleValue + ", cycle " + screen.cycleValue;
    }
}
