package dev.shwg.shwgconfig.gui.components.widget.editbox;

import dev.shwg.shwgconfig.gui.deferred.DeferredElement;
import dev.shwg.shwgconfig.gui.deferred.GenericTooltip;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.render.GenericCursors;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.GenericIcons;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class NumberEditBox<N extends Number> extends GenericAbstractEditBox {

    private static final int BORDER_COLOR = 0xFFFF4F4F;
    private static final int HINT_TEXT_COLOR = 0xFFFF4F4F;
    private static final int HINT_PADDING = 3;
    private static final int HINT_GAP = 4;
    private static final int LINE_HEIGHT = 10;

    private static final int ARROW_ZONE_WIDTH = GenericIcons.SMALL_ARROW_GLYPH_WIDTH + 4;

    private enum Violation { NONE, BELOW_MIN, ABOVE_MAX, INVALID }

    private final double min;
    private final double max;
    private final double step;
    private final boolean allowDecimals;
    private final Function<Double, N> fromDouble;
    private final Function<N, String> formatter;
    private final Consumer<N> applyValue;

    private double validRaw;
    private Violation violation = Violation.NONE;

    public NumberEditBox(Font font, int x, int y, int width, int height, Component message,
                         double min, double max, double step, N initialValue, boolean allowDecimals,
                         Function<Double, N> fromDouble, Function<N, String> formatter, Consumer<N> applyValue) {
        super(font, x, y, width, height, message);
        this.min = min;
        this.max = max;
        this.step = step;
        this.allowDecimals = allowDecimals;
        this.fromDouble = fromDouble;
        this.formatter = formatter;
        this.applyValue = applyValue;
        this.validRaw = Mth.clamp(initialValue.doubleValue(), min, max);
        setFilter(this::isValidPartialInput);
        setResponder(this::onTextChanged);
        setValue(formatter.apply(fromDouble.apply(validRaw)));
    }

    private boolean isValidPartialInput(String text) {
        if (text.isEmpty()) return true;
        return allowDecimals ? text.matches("-?\\d*\\.?\\d*") : text.matches("-?\\d*");
    }

    private void onTextChanged(String text) {
        Double parsed = tryParse(text);
        if (parsed == null) {
            violation = Violation.INVALID;
        } else if (parsed < min) {
            violation = Violation.BELOW_MIN;
        } else if (parsed > max) {
            violation = Violation.ABOVE_MAX;
        } else {
            violation = Violation.NONE;
            validRaw = parsed;
            applyValue.accept(fromDouble.apply(validRaw));
        }
    }

    private @Nullable Double tryParse(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void onFocusLost() {
        if (violation != Violation.NONE) {
            Double parsed = tryParse(getValue());
            validRaw = parsed == null ? validRaw : Mth.clamp(parsed, min, max);
            N value = fromDouble.apply(validRaw);
            setValue(formatter.apply(value));
            applyValue.accept(value);
            violation = Violation.NONE;
        }
    }

    private void adjustValue(int direction) {
        double newRaw = Mth.clamp(validRaw + direction * step, min, max);
        N value = fromDouble.apply(newRaw);
        setValue(formatter.apply(value));
    }

    private List<Component> buildHintLines() {

        Component minComponent = Component.translatable("shwgconfig.number.min").append(": ").append(formatter.apply(fromDouble.apply(min)));
        Component maxComponent = Component.translatable("shwgconfig.number.max").append(": ").append(formatter.apply(fromDouble.apply(max)));

        return switch (violation) {
//            case BELOW_MIN -> List.of(Component.literal("Min: " + formatter.apply(fromDouble.apply(min))));
            case BELOW_MIN -> List.of(minComponent);
            case ABOVE_MAX -> List.of(maxComponent);
            default -> List.of(minComponent, maxComponent);
        };
    }

    private int getArrowZoneX() {
        return getX() + getWidth() - ARROW_ZONE_WIDTH;
    }

    private int getArrowHalfHeight() {
        return getHeight() / 2;
    }

    private boolean isOverUpArrow(double mouseX, double mouseY) {
        return mouseX > getArrowZoneX() && mouseX <= getArrowZoneX() + GenericIcons.SMALL_ARROW_GLYPH_WIDTH + 1
                && mouseY >= getY() + getArrowHalfHeight() - GenericIcons.SMALL_ARROW_GLYPH_HEIGHT && mouseY < getY() + getArrowHalfHeight();
    }

    private boolean isOverDownArrow(double mouseX, double mouseY) {
        return mouseX > getArrowZoneX() && mouseX <= getArrowZoneX() + GenericIcons.SMALL_ARROW_GLYPH_WIDTH + 1
                && mouseY >= getY() + getArrowHalfHeight() && mouseY < getY() + getArrowHalfHeight() + GenericIcons.SMALL_ARROW_GLYPH_HEIGHT;
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent event) {
        if (isOverUpArrow(event.getMouseX(), event.getMouseY())) {
            adjustValue(1);
            return true;
        }
        if (isOverDownArrow(event.getMouseX(), event.getMouseY())) {
            adjustValue(-1);
            return true;
        }
        return super.onMouseClicked(event);
    }

    @Override
    protected boolean onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (isOverUpArrow(mouseX, mouseY) || isOverDownArrow(mouseX, mouseY)) {
            adjustValue((int) vertical);
            return true;
        }
        return super.onMouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        if (keyEvent.isUp()) {
            adjustValue(1);
            return true;
        }
        if (keyEvent.isDown()) {
            adjustValue(-1);
            return true;
        }
        return super.onKeyPressed(keyEvent);
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        renderArrows(graphics, mouseX, mouseY);
        if (violation != Violation.NONE) {
            graphics.outline(this, BORDER_COLOR);
            graphics.addDeferred(new HintElement());
        }
    }


    private void renderArrows(GenericGraphics graphics, int mouseX, int mouseY) {
        int zoneX = getArrowZoneX();
        int halfHeight = getArrowHalfHeight();

        boolean overUp = isOverUpArrow(mouseX, mouseY);
        boolean overDown = isOverDownArrow(mouseX, mouseY);

        if (overUp || overDown) {
            graphics.requestCursor(GenericCursors.POINTING_HAND);
        }

        int glyphX = zoneX + (ARROW_ZONE_WIDTH - GenericIcons.SMALL_ARROW_GLYPH_WIDTH) / 2;
        int upGlyphY = getY() + (halfHeight - GenericIcons.SMALL_ARROW_GLYPH_HEIGHT);
        int downGlyphY = getY() + halfHeight;

        GenericIcons.drawArrowUp(graphics, glyphX, upGlyphY, overUp);
        GenericIcons.drawArrowDown(graphics, glyphX, downGlyphY, overDown);
    }

    private class HintElement implements DeferredElement {
        @Override
        public void render(GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
            List<Component> lines = buildHintLines();

            int textWidth = 0;
            for (Component line : lines) {
                textWidth = Math.max(textWidth, font.width(line));
            }
            int textHeight = (lines.size() - 1) * LINE_HEIGHT + 8;

            int bgWidth = textWidth + HINT_PADDING * 2;
            int bgHeight = textHeight + HINT_PADDING * 2;

            int bgX = getX() - HINT_GAP - bgWidth;
            int bgY = getY() + (getHeight() - bgHeight) / 2;

            GenericTooltip.renderAdjustedTooltip(graphics, bgX, bgY, bgWidth, bgHeight);

            int textX = bgX + HINT_PADDING;
            int textY = bgY + HINT_PADDING;
            for (int i = 0; i < lines.size(); i++) {
                graphics.drawString(font, lines.get(i), textX, textY + i * LINE_HEIGHT, HINT_TEXT_COLOR);
            }
        }
    }

    public static Builder<Integer> intEditBox(Font font, Component message) {
        return new Builder<>(font, message, false, d -> (int) Math.round(d), String::valueOf, 1.0);
    }

    public static Builder<Float> floatEditBox(Font font, Component message) {
        return new Builder<>(font, message, true, Double::floatValue, String::valueOf, 1.0);
    }

    public static Builder<Double> doubleEditBox(Font font, Component message) {
        return new Builder<>(font, message, true, d -> d, String::valueOf, 1.0);
    }

    public static class Builder<N extends Number> extends GenericAbstractEditBox.Builder<NumberEditBox<N>, Builder<N>> {

        private final boolean allowDecimals;
        private final Function<Double, N> fromDouble;
        private double min = -Double.MAX_VALUE;
        private double max = Double.MAX_VALUE;
        private double step;
        private N initialValue;
        private Function<N, String> formatter;
        private Consumer<N> applyValue = v -> {};

        private Builder(Font font, Component message, boolean allowDecimals,
                        Function<Double, N> fromDouble, Function<N, String> defaultFormatter, double defaultStep) {
            super(font, message);
            this.allowDecimals = allowDecimals;
            this.fromDouble = fromDouble;
            this.formatter = defaultFormatter;
            this.step = defaultStep;
            this.initialValue = fromDouble.apply(0.0);
        }

        public Builder<N> min(N min) {
            this.min = min.doubleValue();
            return self();
        }

        public Builder<N> max(N max) {
            this.max = max.doubleValue();
            return self();
        }

        public Builder<N> range(N min, N max) {
            this.min = min.doubleValue();
            this.max = max.doubleValue();
            return self();
        }

        public Builder<N> step(N step) {
            this.step = step.doubleValue();
            return self();
        }

        public Builder<N> initialValue(N initialValue) {
            this.initialValue = initialValue;
            return self();
        }

        public Builder<N> formatter(Function<N, String> formatter) {
            this.formatter = formatter;
            return self();
        }

        public Builder<N> decimals(int decimals) {
            this.formatter = v -> String.format("%." + decimals + "f", v.doubleValue());
            return self();
        }

        public Builder<N> applyValue(Consumer<N> applyValue) {
            this.applyValue = applyValue;
            return self();
        }

        @Override
        public Builder<N> filter(Predicate<String> filter) {
            return self();
        }

        @Override
        public Builder<N> responder(Consumer<String> responder) {
            return self();
        }

        @Override
        public NumberEditBox<N> build() {
            NumberEditBox<N> editBox = new NumberEditBox<>(font, x, y, width, height, message,
                    min, max, step, initialValue, allowDecimals, fromDouble, formatter, applyValue);
            editBox.setMaxLength(maxLength);
            if (hint != null) {
                editBox.setHint(hint);
            }
            editBox.setBordered(bordered);
            editBox.setEditable(editable);
            return editBox;
        }
    }
}
