package dev.shwg.testmod.test.interaction;

import dev.shwg.testmod.test.render.WidgetGallery;
import dev.shwg.shwgconfig.gui.components.screens.GenericScreen;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.components.widget.button.CycleButton;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericButton;
import dev.shwg.shwgconfig.gui.components.widget.editbox.GenericAbstractEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.GenericEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.HexColorEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.NumberEditBox;
import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The screen the interaction test ({@link InteractionTest}) clicks and types on. Unlike the
 * {@link WidgetGallery}, its widgets are really added to the screen, because they have to receive input.
 *
 * <p>Every widget records what it applied, so a check can compare the value directly. Nothing here goes through
 * TestConfig. The screen uses a virtual mouse: rendering and hovering follow the position of the last simulated
 * click, never the real cursor, so screenshots don't depend on where the cursor happens to be.</p>
 *
 * <p>Two columns: buttons on the left (part 3a), text inputs on the right (part 3b). Between them, at
 * {@link #EMPTY_X}/{@link #EMPTY_Y}, is a spot without any widget, for "clicking next to it" checks.</p>
 */
public class InteractionTestScreen extends GenericScreen {

    private static final int[] LABEL_X = {10, 240}, WIDGET_X = {90, 340};
    private static final int WIDGET_W = 110, WIDGET_H = 20;
    private static final int FIRST_ROW_Y = 40, ROW_H = 30;

    /** A spot between the two columns where no widget is. */
    static final int EMPTY_X = 220, EMPTY_Y = 255;
    private static final int BG = 0xFF101010, TITLE = 0xFFFFFFFF, LABEL = 0xFFFFFFFF;

    private Runnable tickHook;
    private int virtualX = -1, virtualY = -1;
    private record Label(String text, int x, int y) {
    }

    private final List<Label> labels = new ArrayList<>();
    private final int[] rowsInColumn = new int[2];

    GenericButton button;
    CycleButton<Boolean> toggle;
    CycleButton<String> cycle;

    GenericEditBox textBox;
    NumberEditBox<Integer> intBox;
    NumberEditBox<Float> floatBox;
    NumberEditBox<Double> doubleBox;
    HexColorEditBox argbBox;
    HexColorEditBox rgbBox;

    int buttonPresses;
    boolean toggleValue;
    String cycleValue;

    String textValue;
    int intValue;
    float floatValue;
    double doubleValue;
    int argbValue;
    int rgbValue;

    public InteractionTestScreen(Screen parent) {
        super(Component.literal("Interaction Test"), parent);
    }

    @Override
    protected void init() {
        super.init();
        labels.clear();
        rowsInColumn[0] = rowsInColumn[1] = 0;

        buttonPresses = 0;
        toggleValue = true;
        cycleValue = "Banana";
        button = addRow(0, "Button", GenericButton.builder(Component.literal("Press me"), b -> buttonPresses++));
        toggle = addRow(0, "Toggle", CycleButton.of(List.of(true, false), on -> Component.literal(on ? "ON" : "OFF"), true)
                .applyValue(value -> toggleValue = value));
        cycle = addRow(0, "Cycle", CycleButton.of(List.of("Apple", "Banana", "Cherry"), Component::literal, "Banana")
                .applyValue(value -> cycleValue = value));

        textValue = "Hello";
        intValue = 42;
        floatValue = 0.25f;
        doubleValue = 7.5;
        argbValue = 0xFF000000;
        rgbValue = 0xFF000000;
        textBox = addRow(1, "Text", GenericEditBox.builder(font, Component.literal("Text")));
        textBox.setValue(textValue);
        textBox.setResponder(value -> textValue = value);
        intBox = addRow(1, "Int 0-100", NumberEditBox.intEditBox(font, Component.literal("Int"))
                .range(0, 100).initialValue(42).applyValue(value -> intValue = value));
        floatBox = addRow(1, "Float 0-1", NumberEditBox.floatEditBox(font, Component.literal("Float"))
                .range(0f, 1f).initialValue(0.25f).decimals(2).applyValue(value -> floatValue = value));
        doubleBox = addRow(1, "Double 0-10", NumberEditBox.doubleEditBox(font, Component.literal("Double"))
                .range(0.0, 10.0).initialValue(7.5).decimals(2).applyValue(value -> doubleValue = value));
        argbBox = addRow(1, "Color ARGB", HexColorEditBox.argb(font, Component.literal("ARGB"))
                .setColor(0xFF000000).applyValue(value -> argbValue = value));
        rgbBox = addRow(1, "Color RGB", HexColorEditBox.rgb(font, Component.literal("RGB"))
                .setColor(0x000000).applyValue(value -> rgbValue = value));
    }

    /** Builds the widget into the next row of {@code column} and adds it to the screen, so it receives input. */
    private <W extends GenericAbstractWidget> W addRow(int column, String label, GenericAbstractWidget.Builder<W, ?> builder) {
        int y = FIRST_ROW_Y + rowsInColumn[column]++ * ROW_H;
        W widget = builder.bounds(WIDGET_X[column], y, WIDGET_W, WIDGET_H).build();
        labels.add(new Label(label, LABEL_X[column], y));
        addAndRegister(widget);
        return widget;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickHook != null) tickHook.run();
    }

    public void setTickHook(Runnable hook) {
        this.tickHook = hook;
    }

    // Rendering, with the virtual mouse instead of the real one

    @Override
    public void render(GenericGraphics g, int mouseX, int mouseY, float partialTicks) {
        g.fill(0, 0, width, height, BG);
        super.render(g, virtualX, virtualY, partialTicks);

        Font font = Minecraft.getInstance().font;
        g.drawString(font, "Interaction test", 10, 8, TITLE);
        for (Label label : labels) {
            g.drawString(font, label.text(), label.x(), label.y() + (WIDGET_H - font.lineHeight) / 2 + 1, LABEL, false);
        }
    }

    /**
     * A solid color instead of vanilla's background (dirt, blurred panorama, ...), so the screenshots
     * look the same on every version. Before 1.21.6 GenericScreen.render calls this.
     */
    @Override
    public void renderBackground(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(0, 0, width, height, BG);
    }

    @Override
    public void renderDeferred(GenericGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderDeferred(graphics, virtualX, virtualY, partialTicks);
    }

    // Simulated input, used by the steps in InteractionTest

    /** Left click in the middle of the widget. */
    void click(GenericAbstractWidget widget) {
        clickAt(widget.getX() + widget.getWidth() / 2, widget.getY() + widget.getHeight() / 2, InputSim.LEFT);
    }

    /** Right click in the middle of the widget. */
    void rightClick(GenericAbstractWidget widget) {
        clickAt(widget.getX() + widget.getWidth() / 2, widget.getY() + widget.getHeight() / 2, InputSim.RIGHT);
    }

    /** Moves the virtual mouse to (x, y) and clicks there. */
    void clickAt(int x, int y, int button) {
        virtualX = x;
        virtualY = y;
        InputSim.click(this, x, y, button);
    }

    void key(int keyCode) {
        InputSim.key(this, keyCode);
    }

    void type(String text) {
        InputSim.type(this, text);
    }

    /**
     * Clicks into the field, moves the cursor to the end and deletes everything with Backspace, like a user would.
     * Ctrl+A isn't used: before 1.21.9 vanilla reads Ctrl from the real keyboard, not from the key event.
     * A color field keeps its "#", which can't be deleted.
     */
    void clear(GenericAbstractEditBox box) {
        click(box);
        key(InputConstants.KEY_END);
        int length = box.getValue().length();
        for (int i = 0; i < length; i++) {
            key(InputConstants.KEY_BACKSPACE);
        }
    }
}
