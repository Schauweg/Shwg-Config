package dev.shwg.testmod.test.render;

import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.components.widget.button.CycleButton;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericButton;
import dev.shwg.shwgconfig.gui.components.widget.button.GenericKeyBindButton;
import dev.shwg.shwgconfig.gui.components.widget.dropdownwidget.DropdownWidget;
import dev.shwg.shwgconfig.gui.components.widget.editbox.GenericEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.HexColorEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.NumberEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionEditBox;
import dev.shwg.shwgconfig.gui.components.widget.editbox.suggestioneditbox.SuggestionProviders;
import dev.shwg.shwgconfig.gui.components.widget.slider.NumberSlider;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class WidgetGallery {

    private static final String[] STATES = {"normal", "hovered", "focused", "disabled"};
    private static final int FOCUSED = 2, DISABLED = 3;

    private static final int LABEL_X = 10, LABEL_W = 92;
    private static final int COL_W = 88, COL_GAP = 4;
    private static final int HEADER_Y = 26, FIRST_ROW_Y = 38, ROW_H = 28, WIDGET_H = 20;

    private static final int LABEL = 0xFFFFFFFF, HINT = 0xFF909090, ROW_LINE = 0xFF303030;

    private enum Fruit {APPLE, BANANA, CHERRY}

    private record Row(String label, List<GenericAbstractWidget> widgets) {
    }

    private final List<Row> buttonsAndSliders = new ArrayList<>();
    private final List<Row> inputs = new ArrayList<>();
    private final List<Row> dropdownsAndSearch = new ArrayList<>();

    public WidgetGallery(Font font) {
        KeyMapping jump = Minecraft.getInstance().options.keyJump;

        add(buttonsAndSliders, "Button", () -> GenericButton.builder(Component.literal("Button"), button -> {
        }));
        add(buttonsAndSliders, "Toggle", () -> CycleButton.of(List.of(true, false),
                on -> Component.literal(on ? "ON" : "OFF"), true));
        add(buttonsAndSliders, "Cycle", () -> CycleButton.of(List.of("Apple", "Banana", "Cherry"),
                Component::literal, "Banana"));
        add(buttonsAndSliders, "Keybind", () -> new GenericKeyBindButton.Builder(jump.getTranslatedKeyMessage(), jump));
        add(buttonsAndSliders, "Int slider", () -> NumberSlider.intSlider(Component.literal("Int"), 0, 100, 42));
        add(buttonsAndSliders, "Float slider", () -> NumberSlider.floatSlider(Component.literal("Float"), 0f, 1f, 0.25f, 0.05f));
        add(buttonsAndSliders, "Double slider", () -> NumberSlider.doubleSlider(Component.literal("Double"), 0, 10, 7.5, 0.5));

        add(inputs, "Text", () -> GenericEditBox.builder(font, Component.literal("Text")),
                box -> box.setValue("Hello world"));
        add(inputs, "Text (hint)", () -> GenericEditBox.builder(font, Component.literal("Hint")).hint("Type here..."));
        add(inputs, "Int input", () -> NumberEditBox.intEditBox(font, Component.literal("Int"))
                .range(0, 100).initialValue(42));
        add(inputs, "Float input", () -> NumberEditBox.floatEditBox(font, Component.literal("Float"))
                .range(0f, 1f).initialValue(0.25f).decimals(2));
        add(inputs, "Double input", () -> NumberEditBox.doubleEditBox(font, Component.literal("Double"))
                .range(0.0, 10.0).initialValue(7.5).decimals(1));
        add(inputs, "Color ARGB", () -> HexColorEditBox.argb(font, Component.literal("ARGB")).setColor(0x80FF8800));
        add(inputs, "Color RGB", () -> HexColorEditBox.rgb(font, Component.literal("RGB")).setColor(0x3399FF));

        add(dropdownsAndSearch, "String dropdown", () -> DropdownWidget.ofStrings(List.of("Apple", "Banana", "Cherry"))
                .value("Banana"));
        add(dropdownsAndSearch, "Enum dropdown", () -> DropdownWidget.ofEnum(Fruit.class,
                        fruit -> Component.literal(fruit.name().charAt(0) + fruit.name().substring(1).toLowerCase(java.util.Locale.ROOT)))
                .value(Fruit.CHERRY));
        add(dropdownsAndSearch, "Text search", () -> SuggestionEditBox.builder(font, Component.literal("Text"),
                SuggestionProviders.strings(List.of("Apple", "Banana", "Cherry"))).value("Banana"));
        add(dropdownsAndSearch, "Item search", () -> SuggestionEditBox.builder(font, Component.literal("Item"),
                SuggestionProviders.ITEMS).value("minecraft:diamond"));
    }

    /** Page "Buttons & sliders". */
    public void renderButtonsAndSliders(GenericGraphics g, Font font, float partialTicks) {
        renderPage(g, font, buttonsAndSliders, partialTicks);
    }

    /** Page "Inputs". */
    public void renderInputs(GenericGraphics g, Font font, float partialTicks) {
        renderPage(g, font, inputs, partialTicks);
    }

    /** Page "Dropdowns & search". */
    public void renderDropdownsAndSearch(GenericGraphics g, Font font, float partialTicks) {
        renderPage(g, font, dropdownsAndSearch, partialTicks);
    }

    // Building

    private static <W extends GenericAbstractWidget> void add(List<Row> page, String label,
                                                              Supplier<? extends GenericAbstractWidget.Builder<W, ?>> builder) {
        add(page, label, builder, widget -> {
        });
    }

    private static <W extends GenericAbstractWidget> void add(List<Row> page, String label,
                                                              Supplier<? extends GenericAbstractWidget.Builder<W, ?>> builder,
                                                              Consumer<? super W> setup) {
        int widgetY = FIRST_ROW_Y + page.size() * ROW_H + (ROW_H - WIDGET_H) / 2;
        List<GenericAbstractWidget> widgets = new ArrayList<>();
        for (int state = 0; state < STATES.length; state++) {
            W widget = builder.get().bounds(columnX(state), widgetY, COL_W, WIDGET_H).build();
            setup.accept(widget);
            if (state == FOCUSED) {
                widget.setFocused(true);
                // Focusing a suggestion box can open its list, depending on the last input (LastInput).
                // The list belongs to step 3 (interaction), here it must stay closed in every run.
                if (widget instanceof SuggestionEditBox box) box.getOverlay().open(false);
            }
            if (state == DISABLED) widget.active = false;
            widgets.add(widget);
        }
        page.add(new Row(label, widgets));
    }

    private static int columnX(int state) {
        return LABEL_X + LABEL_W + state * (COL_W + COL_GAP);
    }

    // Rendering

    private static void renderPage(GenericGraphics g, Font font, List<Row> rows, float partialTicks) {
        for (int state = 0; state < STATES.length; state++) {
            g.drawCenteredString(font, STATES[state], columnX(state) + COL_W / 2, HEADER_Y, HINT, false);
        }

        int right = columnX(STATES.length - 1) + COL_W;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int rowY = FIRST_ROW_Y + i * ROW_H;
            g.hLine(LABEL_X, right - 1, rowY + ROW_H - 1, ROW_LINE);
            g.drawString(font, row.label(), LABEL_X, rowY + (ROW_H - font.lineHeight) / 2, LABEL, false);

            for (int state = 0; state < row.widgets().size(); state++) {
                GenericAbstractWidget widget = row.widgets().get(state);
                boolean hovered = state == 1;
                int mouseX = hovered ? widget.getX() + widget.getWidth() / 2 : -1;
                int mouseY = hovered ? widget.getY() + widget.getHeight() / 2 : -1;
                widget.render(g, mouseX, mouseY, partialTicks);
            }
        }
    }
}
